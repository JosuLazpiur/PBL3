#include <nireGpio.h>

void gpioClockPIztu(GPIO_TypeDef *gpio){
	uint32_t gpioint = (int) gpio;
	uint32_t gpioAint = (int) GPIOA;
	uint8_t GPIObit = (gpioint- gpioAint) / 0x400;

	RCC->AHB1ENR |= 1<<GPIObit;

}

void gpioPinKonfiguratu(GPIO_TypeDef *gpio, uint8_t pin, uint8_t mode) {
    gpio->MODER &= ~(3 << (pin * 2));
    if (mode == OUT)
        gpio->MODER |= 1 << (pin * 2);
    if (mode == ALTERN)
    	gpio->MODER |= 2 << (pin *2);
    if (mode == ANALOG)
    	gpio->MODER |= 3 << (pin *2);
}

void gpio_Piztu(GPIO_TypeDef *gpio, uint8_t pin){
	gpio->ODR |= 1<<pin;
}

void gpio_Itzali(GPIO_TypeDef *gpio, uint8_t pin){
	gpio->ODR &= ~(1<<pin);
}

uint32_t gpio_Irakurri(GPIO_TypeDef *gpio){

	return gpio->IDR;
}
