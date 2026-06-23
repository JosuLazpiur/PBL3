#include <adc.h>

// ADC 1 CHANNEL 3
//
//GPIO PA3

void adcHasieratu(GPIO_TypeDef *gpio,uint8_t PIN_ADC, ADC_TypeDef* adc, uint8_t channel){
	gpioClockPIztu(gpio);
	gpioPinKonfiguratu(gpio, PIN_ADC, ANALOG);
	adcClockPiztu();
	adcPiztu(adc);
	adcErresoluzioaKonfiguratu(adc);
	adcSingleConversion(adc, PIZTU);
	adcKanala(adc, channel);
	//adcEtenduraKonfiguratu(ADC3);
}


void adcClockPiztu(){
	RCC->APB2ENR |= 1<<8;
}

void adcErresoluzioaKonfiguratu(ADC_TypeDef *adc){
	adc->CR1 &= ~(3<<PIN_ADC_CR1_RES);
}

void adcPiztu(ADC_TypeDef *adc){
	adc->CR2 |=  (1<<PIN_ADC_CR2_ADON);
}

void adcSingleConversion(ADC_TypeDef *adc, uint8_t mode){
	if(mode==ITZALI)
		adc->CR2 |= 1<<PIN_ADC_CR2_CONT;
	else if(mode==PIZTU)
		adc->CR2 &= ~(1<<PIN_ADC_CR2_CONT);
}
void adcKanala(ADC_TypeDef *adc, uint8_t channel){
	adc->SQR1 &= ~(0xF<<PIN_ADC_SQR1_L);
	adc->SQR1 |= (0 << 20);

	adc->SQR3 &= ~(0x1F << PIN_ADC_KANAL);
	adc->SQR3 |= channel << PIN_ADC_KANAL;
}

void startConversion(ADC_TypeDef *adc){
	adc->CR2 |= (1<<PIN_ADC_CR2_SWSTART);
}
uint32_t amaieraKonprobatu(ADC_TypeDef *adc){
	uint32_t bit;
	bit = adc->SR & (1<<1);
	bit = bit>>1;

	return bit;
}
uint32_t adcIrakurri(ADC_TypeDef *adc){
	uint32_t erantzuna;

	erantzuna = adc->DR & 0xFFFF;
	return  erantzuna;

}
void adcEtenduraKonfiguratu(ADC_TypeDef *adc){
	adc->CR1 |= 1<<5;
	NVIC->ISER[0] |= 1<<18;
}
void adcEtendura(void){
	uint16_t a = ADC3->DR;

}
