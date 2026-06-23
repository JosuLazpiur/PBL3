/* USER CODE BEGIN Header */
/**
  ******************************************************************************
  * @file    app_netxduo.c
  * @author  MCD Application Team
  * @brief   NetXDuo applicative file
  ******************************************************************************
  * @attention
  *
  * Copyright (c) 2021 STMicroelectronics.
  * All rights reserved.
  *
  * This software is licensed under terms that can be found in the LICENSE file
  * in the root directory of this software component.
  * If no LICENSE file comes with this software, it is provided AS-IS.
  *
  ******************************************************************************
  */
/* USER CODE END Header */

/* Includes ------------------------------------------------------------------*/
#include "app_netxduo.h"

/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */
#include "nx_ip.h"
#include "nx_stm32_eth_config.h"
#include "leda.h"
#include  MOSQUITTO_CERT_FILE
/* USER CODE END Includes */

/* Private typedef -----------------------------------------------------------*/
/* USER CODE BEGIN PTD */

extern RNG_HandleTypeDef hrng;
extern TX_QUEUE QueueGas;
extern TX_QUEUE QueueInfrarojo;

TX_THREAD AppMainThread;
TX_THREAD AppMQTTClientThread;
TX_THREAD AppLinkThread;

TX_SEMAPHORE Semaphore;

NX_PACKET_POOL  AppPool;
NX_IP           IpInstance;
NX_DHCP         DHCPClient;
NXD_MQTT_CLIENT mqtt_client;
static NX_DNS   dns_client;

ULONG   IpAddress;
ULONG   NetMask;

ULONG mqtt_client_stack[MQTT_CLIENT_STACK_SIZE];

TX_EVENT_FLAGS_GROUP mqtt_app_flag;

/* Declare buffers to hold message and topic. */
static char message[NXD_MQTT_MAX_MESSAGE_LENGTH];
static UCHAR message_buffer[NXD_MQTT_MAX_MESSAGE_LENGTH];
static UCHAR topic_buffer[NXD_MQTT_MAX_TOPIC_NAME_LENGTH];

/* USER CODE END PTD */

/* Private define ------------------------------------------------------------*/
/* USER CODE BEGIN PD */

/* USER CODE END PD */

/* Private macro -------------------------------------------------------------*/
/* USER CODE BEGIN PM */

/* USER CODE END PM */

/* Private variables ---------------------------------------------------------*/
/* USER CODE BEGIN PV */
/* USER CODE END PV */

/* Private function prototypes -----------------------------------------------*/
/* USER CODE BEGIN PFP */
static VOID App_Main_Thread_Entry(ULONG thread_input);
static VOID App_MQTT_Client_Thread_Entry(ULONG thread_input);
static VOID App_Link_Thread_Entry(ULONG thread_input);
static VOID ip_address_change_notify_callback(NX_IP *ip_instance, VOID *ptr);
/* USER CODE END PFP */
/**
  * @brief  Application NetXDuo Initialization.
  * @param memory_ptr: memory pointer
  * @retval int
  */
UINT MX_NetXDuo_Init(VOID *memory_ptr)
{
  UINT ret = NX_SUCCESS;
  TX_BYTE_POOL *byte_pool = (TX_BYTE_POOL*)memory_ptr;

  /* USER CODE BEGIN MX_NetXDuo_MEM_POOL */
  /* USER CODE END MX_NetXDuo_MEM_POOL */

  /* USER CODE BEGIN MX_NetXDuo_Init */
  printf("Nx_MQTT_Client application started..\n");

  CHAR *pointer;

  /* Allocate the memory for packet_pool.  */
  if (tx_byte_allocate(byte_pool, (VOID **) &pointer,  NX_PACKET_POOL_SIZE, TX_NO_WAIT) != TX_SUCCESS)
  {
    return TX_POOL_ERROR;
  }

  /* Create the Packet pool to be used for packet allocation */
  ret = nx_packet_pool_create(&AppPool, "Main Packet Pool", PAYLOAD_SIZE, pointer, NX_PACKET_POOL_SIZE);

  if (ret != NX_SUCCESS)
  {
    return NX_NOT_ENABLED;
  }

 /* Allocate the memory for Ip_Instance */
  if (tx_byte_allocate(byte_pool, (VOID **) &pointer, 2 * DEFAULT_MEMORY_SIZE, TX_NO_WAIT) != TX_SUCCESS)
  {
    return TX_POOL_ERROR;
  }

  /* Create the main NX_IP instance */
  ret = nx_ip_create(&IpInstance, "Main Ip instance", NULL_ADDRESS, NULL_ADDRESS, &AppPool, nx_stm32_eth_driver,
                     pointer, 2 * DEFAULT_MEMORY_SIZE, DEFAULT_MAIN_PRIORITY);

  if (ret != NX_SUCCESS)
  {
    return NX_NOT_ENABLED;
  }

    /* create the DHCP client */
  ret = nx_dhcp_create(&DHCPClient, &IpInstance, "DHCP Client");

  if (ret != NX_SUCCESS)
  {
    return NX_NOT_ENABLED;
  }

  /* Allocate the memory for ARP */
  if (tx_byte_allocate(byte_pool, (VOID **) &pointer, DEFAULT_MEMORY_SIZE, TX_NO_WAIT) != TX_SUCCESS)
  {
    return TX_POOL_ERROR;
  }

  /* Enable the ARP protocol and provide the ARP cache size for the IP instance */
  ret = nx_arp_enable(&IpInstance, (VOID *)pointer,  DEFAULT_MEMORY_SIZE);

  if (ret != NX_SUCCESS)
  {
    return NX_NOT_ENABLED;
  }

  /* Enable the ICMP */
  ret = nx_icmp_enable(&IpInstance);

  if (ret != NX_SUCCESS)
  {
    return NX_NOT_ENABLED;
  }

  /* Enable the UDP protocol required for DHCP communication */
  ret = nx_udp_enable(&IpInstance);

  if (ret != NX_SUCCESS)
  {
    return NX_NOT_ENABLED;
  }

  /* Enable the TCP protocol required for DNS, MQTT.. */
  ret = nx_tcp_enable(&IpInstance);

  if (ret != NX_SUCCESS)
  {
    return NX_NOT_ENABLED;
  }

  /* Allocate the memory for main thread   */
  if (tx_byte_allocate(byte_pool, (VOID **) &pointer, THREAD_MEMORY_SIZE, TX_NO_WAIT) != TX_SUCCESS)
  {
    return TX_POOL_ERROR;
  }

  /* Create the main thread */
  ret = tx_thread_create(&AppMainThread, "App Main thread", App_Main_Thread_Entry, 0, pointer, THREAD_MEMORY_SIZE,
                         DEFAULT_MAIN_PRIORITY, DEFAULT_MAIN_PRIORITY, TX_NO_TIME_SLICE, TX_AUTO_START);

  if (ret != TX_SUCCESS)
  {
    return NX_NOT_ENABLED;
  }

  /* Allocate the memory for MQTT client thread   */
  if (tx_byte_allocate(byte_pool, (VOID **) &pointer, THREAD_MEMORY_SIZE, TX_NO_WAIT) != TX_SUCCESS)
  {
    return TX_POOL_ERROR;
  }

  /* create the MQTT client thread */
  ret = tx_thread_create(&AppMQTTClientThread, "App MQTT Thread", App_MQTT_Client_Thread_Entry, 0, pointer, THREAD_MEMORY_SIZE,
                         DEFAULT_PRIORITY, DEFAULT_PRIORITY, TX_NO_TIME_SLICE, TX_DONT_START);

  if (ret != TX_SUCCESS)
  {
    return NX_NOT_ENABLED;
  }

  /* Allocate the memory for Link thread   */
  if (tx_byte_allocate(byte_pool, (VOID **) &pointer,2 *  DEFAULT_MEMORY_SIZE, TX_NO_WAIT) != TX_SUCCESS)
  {
    return TX_POOL_ERROR;
  }

  /* create the Link thread */
  ret = tx_thread_create(&AppLinkThread, "App Link Thread", App_Link_Thread_Entry, 0, pointer, 2 * DEFAULT_MEMORY_SIZE,
                         LINK_PRIORITY, LINK_PRIORITY, TX_NO_TIME_SLICE, TX_AUTO_START);

  if (ret != TX_SUCCESS)
  {
    return NX_NOT_ENABLED;
  }


  /* set DHCP notification callback  */

  tx_semaphore_create(&Semaphore, "DHCP Semaphore", 0);
  /* USER CODE END MX_NetXDuo_Init */

  return ret;
}

/* USER CODE BEGIN 1 */

/**
* @brief  ip address change callback.
* @param ip_instance: NX_IP instance
* @param ptr: user data
* @retval none
*/
static VOID ip_address_change_notify_callback(NX_IP *ip_instance, VOID *ptr)
{
  /* release the semaphore as soon as an IP address is available */
  tx_semaphore_put(&Semaphore);
}

/**
* @brief  Main thread entry.
* @param thread_input: ULONG user argument used by the thread entry
* @retval none
*/
static VOID App_Main_Thread_Entry(ULONG thread_input)
{
  UINT ret = NX_SUCCESS;

  ret = nx_ip_address_change_notify(&IpInstance, ip_address_change_notify_callback, NULL);
  if (ret != NX_SUCCESS)
  {
    Error_Handler();
  }

  /* start DHCP client */
  ret = nx_dhcp_start(&DHCPClient);
  if (ret != NX_SUCCESS)
  {
    Error_Handler();
  }

  /* wait until an IP address is ready */
  if(tx_semaphore_get(&Semaphore, TX_WAIT_FOREVER) != TX_SUCCESS)
  {
    Error_Handler();
  }

  ret = nx_ip_address_get(&IpInstance, &IpAddress, &NetMask);

  if (ret != TX_SUCCESS)
  {
    Error_Handler();
  }

 PRINT_IP_ADDRESS(IpAddress);

  /* start the MQTT client thread */
  tx_thread_resume(&AppMQTTClientThread);

  /* this thread is not needed any more, we relinquish it */
  tx_thread_relinquish();

  return;
}

/* Declare the disconnect notify function. */
static VOID my_disconnect_func(NXD_MQTT_CLIENT *client_ptr)
{
  NX_PARAMETER_NOT_USED(client_ptr);
  printf("client disconnected from broker < %s >.\n", MQTT_BROKER_NAME);
}

/* Declare the notify function. */
static VOID my_notify_func(NXD_MQTT_CLIENT* client_ptr, UINT number_of_messages)
{
  NX_PARAMETER_NOT_USED(client_ptr);
  NX_PARAMETER_NOT_USED(number_of_messages);
  tx_event_flags_set(&mqtt_app_flag, DEMO_MESSAGE_EVENT, TX_OR);
  return;
}

/**
* @brief  message generation Function.
* @param  RandomNbr
* @retval none
*/
void message_generate(uint32_t *RandomNbr)
{
  HAL_RNG_Init(&hrng);

  /* generate a random number */
  if(HAL_RNG_GenerateRandomNumber(&hrng, RandomNbr) != HAL_OK)
  {
    Error_Handler();
  }

  *RandomNbr %= 100;
}

/* Callback to setup TLS parameters for secure MQTT connection. */
//UINT tls_setup_callback(NXD_MQTT_CLIENT *client_pt,
//                        NX_SECURE_TLS_SESSION *TLS_session_ptr,
//                        NX_SECURE_X509_CERT *certificate_ptr,
//                        NX_SECURE_X509_CERT *trusted_certificate_ptr)
//{
//  UINT ret = NX_SUCCESS;
//  NX_PARAMETER_NOT_USED(client_pt);
//
//  /* Initialize TLS module */
//  nx_secure_tls_initialize();
//
//  /* Create a TLS session */
//  ret = nx_secure_tls_session_create(TLS_session_ptr, &nx_crypto_tls_ciphers,
//                                     crypto_metadata_client, sizeof(crypto_metadata_client));
//  if (ret != TX_SUCCESS)
//  {
//    Error_Handler();
//  }
//  /* Need to allocate space for the certificate coming in from the broker. */
//  memset((certificate_ptr), 0, sizeof(NX_SECURE_X509_CERT));
//
//  /* Allocate space for packet reassembly. */
//  ret = nx_secure_tls_session_packet_buffer_set(TLS_session_ptr, tls_packet_buffer,
//                                                sizeof(tls_packet_buffer));
//  if (ret != TX_SUCCESS)
//  {
//    Error_Handler();
//  }
//
//  /* allocate space for the certificate coming in from the remote host */
//  ret = nx_secure_tls_remote_certificate_allocate(TLS_session_ptr, certificate_ptr,
//                                                  tls_packet_buffer, sizeof(tls_packet_buffer));
//  if (ret != TX_SUCCESS)
//  {
//    Error_Handler();
//  }
//
//  /* initialize Certificate to verify incoming server certificates. */
//  ret = nx_secure_x509_certificate_initialize(trusted_certificate_ptr, (UCHAR*)mosquitto_org_der,
//                                              mosquitto_org_der_len, NX_NULL, 0, NULL, 0,
//                                              NX_SECURE_X509_KEY_TYPE_NONE);
//  if (ret != TX_SUCCESS)
//  {
//    printf("Certificate issue..\nPlease make sure that your X509_certificate is valid. \n");
//    Error_Handler();
//  }
//
//  /* Add a CA Certificate to our trusted store */
//  ret = nx_secure_tls_trusted_certificate_add(TLS_session_ptr, trusted_certificate_ptr);
//  if (ret != TX_SUCCESS)
//  {
//    Error_Handler();
//  }
//
//  return ret;
//}

/**
* @brief  MQTT Client thread entry.
* @param thread_input: ULONG user argument used by the thread entry
* @retval none
*/
static VOID App_MQTT_Client_Thread_Entry(ULONG thread_input)
{
  UINT ret = NX_SUCCESS;
  NXD_ADDRESS mqtt_server_ip;
  ULONG events;
  uint32_t gas_sensor_message;
  uint32_t infrarojo_sensor_message;
  UINT topic_length, message_length;
  UINT remaining_msg = NB_MESSAGE;
  UINT message_count = 0;
  UINT unlimited_publish = NX_FALSE;

  mqtt_server_ip.nxd_ip_version = 4;
  mqtt_server_ip.nxd_ip_address.v4 = USER_DNS_ADDRESS;

  /* Create MQTT client instance. */
  ret = nxd_mqtt_client_create(&mqtt_client, "my_client", CLIENT_ID_STRING, STRLEN(CLIENT_ID_STRING),
                               &IpInstance, &AppPool, (VOID*)mqtt_client_stack, MQTT_CLIENT_STACK_SIZE,
                               MQTT_THREAD_PRIORTY, NX_NULL, 0);

  if (ret != NX_SUCCESS)
  {
    Error_Handler();
  }

  /* Register the disconnect notification function. */
  nxd_mqtt_client_disconnect_notify_set(&mqtt_client, my_disconnect_func);

  /* Set the receive notify function. */
  nxd_mqtt_client_receive_notify_set(&mqtt_client, my_notify_func);

  /* Create an MQTT flag */
  ret = tx_event_flags_create(&mqtt_app_flag, "my app event");
  if (ret != TX_SUCCESS)
  {
    Error_Handler();
  }

  /* Start a secure connection to the server. */
  ret = nxd_mqtt_client_connect(&mqtt_client, &mqtt_server_ip, MQTT_PORT, MQTT_KEEP_ALIVE_TIMER, CLEAN_SESSION, NX_WAIT_FOREVER);

  if (ret != NX_SUCCESS)
  {
    printf("\nMQTT client failed to connect to broker < %s >.\n", MQTT_BROKER_NAME);
    Error_Handler();
  }
  else
  {
    printf("\nMQTT client connected to broker < %s > at PORT %d :\n", MQTT_BROKER_NAME, MQTT_PORT);
  }

  /* Subscribe to the topic with QoS level 0. */
  ret = nxd_mqtt_client_subscribe(&mqtt_client, TOPIC_NAME3, STRLEN(TOPIC_NAME3), QOS0);

  if (ret != NX_SUCCESS)
  {
      Error_Handler();
  }

  if (NB_MESSAGE == 0)
      unlimited_publish = NX_TRUE;

  /* Alternating message processing loop. */
  while (unlimited_publish || remaining_msg)
  {
      /* QueueGas kola irakurtzen*/
      if (tx_queue_receive(&QueueGas, &gas_sensor_message, TX_NO_WAIT) == TX_SUCCESS)
      {
          snprintf(message, STRLEN(message), "%lu", (unsigned long)gas_sensor_message);

          ret = nxd_mqtt_client_publish(&mqtt_client, TOPIC_NAME1, STRLEN(TOPIC_NAME1),
                                        (CHAR*)message, STRLEN(message), NX_FALSE, QOS1, NX_WAIT_FOREVER);
          if (ret != NX_SUCCESS)
          {
              Error_Handler();
          }

          message_count++;
          remaining_msg--;
      }

      /* QueueInfrarojo kola irakurtzen*/
      if (tx_queue_receive(&QueueInfrarojo, &infrarojo_sensor_message, TX_NO_WAIT) == TX_SUCCESS)
      {
          snprintf(message, STRLEN(message), "%lu", (unsigned long)infrarojo_sensor_message);

          ret = nxd_mqtt_client_publish(&mqtt_client, TOPIC_NAME2, STRLEN(TOPIC_NAME2),
                                        (CHAR*)message, STRLEN(message), NX_FALSE, QOS1, NX_WAIT_FOREVER);
          if (ret != NX_SUCCESS)
          {
              Error_Handler();
          }

          message_count++;
          remaining_msg--;
      }

      //Mezua jasotzen badu irakurri
      mezua_Jaso();

      tx_thread_sleep(10);
  }

  /* Now unsubscribe the topic. */
  ret = nxd_mqtt_client_unsubscribe(&mqtt_client, TOPIC_NAME1, STRLEN(TOPIC_NAME1));

  if (ret != NX_SUCCESS)
  {
      Error_Handler();
  }

  /* Disconnect from the broker. */
  ret = nxd_mqtt_client_disconnect(&mqtt_client);

  if (ret != NX_SUCCESS)
  {
      Error_Handler();
  }

  /* Delete the client instance, release all the resources. */
  ret = nxd_mqtt_client_delete(&mqtt_client);

  if (ret != NX_SUCCESS)
  {
      Error_Handler();
  }

  /* test OK -> success Handler */
  Success_Handler();
}

VOID mezua_Jaso()
{
    UINT ret;
    UINT topic_length, message_length;

    /* Leer mensajes de los tópicos suscritos */
    ret = nxd_mqtt_client_message_get(&mqtt_client, topic_buffer, sizeof(topic_buffer), &topic_length,
                                      message_buffer, sizeof(message_buffer), &message_length);

    if (ret == NXD_MQTT_SUCCESS)
    {

        /* Alarma topic-etik jasotako datuak soilik irakurri */
        if (strcmp(topic_buffer, "alarma") == 0)
        {
        	if(strcmp(message_buffer,"1")==0){
        		ledaPiztu(BERDEA);
        	}
        	if(strcmp(message_buffer,"0")==0){
        		ledaItzali(BERDEA);
        	}
        }
    }
}
/**
* @brief  Link thread entry
* @param thread_input: ULONG thread parameter
* @retval none
*/
static VOID App_Link_Thread_Entry(ULONG thread_input)
{
  ULONG actual_status;
  UINT linkdown = 0, status;

  while(1)
  {
    /* Get Physical Link stackavailtus. */
    status = nx_ip_interface_status_check(&IpInstance, 0, NX_IP_LINK_ENABLED,
                                      &actual_status, 10);

    if(status == NX_SUCCESS)
    {
      if(linkdown == 1)
      {
        linkdown = 0;
        status = nx_ip_interface_status_check(&IpInstance, 0, NX_IP_ADDRESS_RESOLVED,
                                      &actual_status, 10);
        if(status == NX_SUCCESS)
        {
          /* The network cable is connected again. */
          printf("The network cable is connected again.\n");
          /* Print MQTT Client is available again. */
          printf("MQTT Client is available again.\n");
        }
        else
        {
          /* The network cable is connected. */
          printf("The network cable is connected.\n");
          /* Send command to Enable Nx driver. */
          nx_ip_driver_direct_command(&IpInstance, NX_LINK_ENABLE,
                                      &actual_status);
          /* Restart DHCP Client. */
          nx_dhcp_stop(&DHCPClient);
          nx_dhcp_start(&DHCPClient);
        }
      }
    }
    else
    {
      if(0 == linkdown)
      {
        linkdown = 1;
        /* The network cable is not connected. */
        printf("The network cable is not connected.\n");
      }
    }

    tx_thread_sleep(100);
  }
}

/* USER CODE END 1 */
