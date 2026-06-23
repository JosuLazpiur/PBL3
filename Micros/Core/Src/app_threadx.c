/* USER CODE BEGIN Header */

/**

  ******************************************************************************

  * @file    app_threadx.c

  * @author  MCD Application Team

  * @brief   ThreadX applicative file

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
#include "app_threadx.h"

/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */



#include <stdint.h>

#include "main.h"
#include "nireGpio.h"
#include "leda.h"
#include "adc.h"
#include "timer.h"

/* USER CODE END Includes */

/* Private typedef -----------------------------------------------------------*/
/* USER CODE BEGIN PTD */



/* USER CODE END PTD */

/* Private define ------------------------------------------------------------*/
/* USER CODE BEGIN PD */

#define THREAD_STACK_SIZE 1024
#define QUEUE_SIZE 1024


#define HASIERATU 0x0

#define ITXARON 0x1

#define BIDALI 0x2
#define ITXARON2 0x3





/* USER CODE END PD */

/* Private macro -------------------------------------------------------------*/
/* USER CODE BEGIN PM */



/* USER CODE END PM */

/* Private variables ---------------------------------------------------------*/
/* USER CODE BEGIN PV */







TX_QUEUE QueueGas;
TX_QUEUE QueueInfrarojo;
ULONG queue_memoryGAS[QUEUE_SIZE];
ULONG queue_memoryINFRAGORRI[QUEUE_SIZE];


uint8_t threadGasStack[THREAD_STACK_SIZE];

TX_THREAD SensorGas;

uint8_t threadInfragorriStack[THREAD_STACK_SIZE];

TX_THREAD SensorInfrarojo;


extern TIM_HandleTypeDef Tim4;


volatile ULONG thread_0_counter = 0;

static volatile uint32_t egoeraGas = 0;

static volatile uint32_t egoeraInfragorri = 0;



/* USER CODE END PV */

/* Private function prototypes -----------------------------------------------*/
/* USER CODE BEGIN PFP */

VOID GasHaria(ULONG parametroa);

VOID InfragorriHaria(ULONG parametroa);

uint32_t adc_to_voltage(uint32_t adc_value);

/* USER CODE END PFP */

/**
  * @brief  Application ThreadX Initialization.
  * @param memory_ptr: memory pointer
  * @retval int
  */
UINT App_ThreadX_Init(VOID *memory_ptr)
{
  UINT ret = TX_SUCCESS;
  TX_BYTE_POOL *byte_pool = (TX_BYTE_POOL*)memory_ptr;

  /* USER CODE BEGIN App_ThreadX_Init */
  (void)byte_pool;

  egoeraGas = HASIERATU;
  egoeraInfragorri = HASIERATU;
  ledakHasieratu();
  ledaPinKonfiguratu(GORRIA);
  ledaPinKonfiguratu(URDINA);

  // Create the queues

  ret = tx_queue_create(&QueueGas, "gas queue", TX_4_ULONG, queue_memoryGAS, sizeof(queue_memoryGAS));
  ret = tx_queue_create(&QueueInfrarojo, "infragorri queue", TX_4_ULONG, queue_memoryINFRAGORRI, sizeof(queue_memoryINFRAGORRI));



  // Create the threads

  tx_thread_create(&SensorGas, "Gas Sentsorea", GasHaria, 0x1234,
                   threadGasStack, THREAD_STACK_SIZE,
                   15, 13,1, TX_AUTO_START);

  tx_thread_create(&SensorInfrarojo, "Infragorri Sentsorea", InfragorriHaria, 0x1234,
                   threadInfragorriStack, THREAD_STACK_SIZE,
                   14, 14, 1, TX_AUTO_START);

  /* USER CODE END App_ThreadX_Init */

  return ret;
}

/**
  * @brief  MX_ThreadX_Init
  * @param  None
  * @retval None
  */
void MX_ThreadX_Init(void)
{
  /* USER CODE BEGIN  Before_Kernel_Start */



  /* USER CODE END  Before_Kernel_Start */

  tx_kernel_enter();

  /* USER CODE BEGIN  Kernel_Start_Error */



  /* USER CODE END  Kernel_Start_Error */
}

/* USER CODE BEGIN 1 */



VOID GasHaria(ULONG parametroa)
{
    volatile uint32_t adc_analogiko = 0;
    volatile uint32_t voltage = 0;
    ULONG start_time = 0;

    while (1)
    {
        switch (egoeraGas) {
            case HASIERATU:
                adcHasieratu(GPIOA, 3, ADC1, 3);
                egoeraGas = ITXARON;
                break;

            case ITXARON:
                ledaItzali(GORRIA);
                startConversion(ADC1);
                if (amaieraKonprobatu(ADC1)) {
                    egoeraGas = BIDALI;
                    //start_time = tx_time_get(); // Momentuko denbora gorda
                }
                break;

            case BIDALI:
                //if (tx_time_get() - start_time >= 60 * TX_TIMER_TICKS_PER_SECOND) { //Irakurritako denborarekin konparatu
                    adc_analogiko = adcIrakurri(ADC1);                              //60s baino handiagoa bada if-ean sartu

                    if (adc_analogiko > 0 && adc_analogiko <= 4095) {
                        voltage = adc_to_voltage(adc_analogiko);
                        ULONG bidaltzekoa = (ULONG)voltage;
                        tx_queue_send(&QueueGas, &bidaltzekoa, TX_NO_WAIT);
                    }

                    egoeraGas = ITXARON2;
                    ledaPiztu(GORRIA);
                //}
                break;
            case ITXARON2:
            	//tx_thread_sleep(90 * TX_TIMER_TICKS_PER_SECOND); // 90 segundu itxaron berriz berotzen hasteko
            	egoeraGas=ITXARON;
            	break;
        }
        tx_thread_sleep(20);
    }
}




VOID InfragorriHaria(ULONG parametroa)

{

	  uint32_t pir_value = 0;

	  while (1)

	  {

	    switch (egoeraInfragorri) {

	      case HASIERATU:

	        gpioClockPIztu(GPIOC);
	        gpioPinKonfiguratu(GPIOC, 3, IN);
	        egoeraInfragorri = ITXARON;

	        break;

	      case ITXARON:

	        pir_value = (gpio_Irakurri(GPIOC) >> 3) & 0x1;
	        if (pir_value == 1 || pir_value==0) {
	          egoeraInfragorri = BIDALI;
	        }
	        ledaItzali(URDINA);

	        break;

	      case BIDALI:

	    	  ULONG bidaltzekoa = (ULONG)pir_value;
	    	  if (bidaltzekoa == 1 || bidaltzekoa == 0) {
	    	      tx_queue_send(&QueueInfrarojo, &bidaltzekoa, TX_NO_WAIT);
	    	      ledaPiztu(URDINA);
	    	  }
	        egoeraInfragorri = ITXARON;
	        break;
	    }

	    tx_thread_sleep(10);
  }

}



uint32_t adc_to_voltage(uint32_t adc_value) {

  uint32_t voltaia = (adc_value * 3300) / 4096;

  return voltaia;

}

/* USER CODE END 1 */
