#ifndef INC_GPIO_H_
#define INC_GPIO_H_

#include <stm32f429xx.h>
#include <stdint.h>


//MODUAK
#define IN 0
#define OUT 1
#define ALTERN 2
#define ANALOG 3

#define ITZALI 0
#define PIZTU  1

void gpioClockPIztu(GPIO_TypeDef *gpio);
void gpioPinKonfiguratu(GPIO_TypeDef *gpio, uint8_t pin, uint8_t mode);
void gpioPiztu(GPIO_TypeDef *gpio, uint8_t pin);
void gpioItzali(GPIO_TypeDef *gpio, uint8_t pin);
uint32_t gpioIrakurri(GPIO_TypeDef *gpio);


#endif
