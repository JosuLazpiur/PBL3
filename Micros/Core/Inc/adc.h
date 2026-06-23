#ifndef INC_ADC_H_
#define INC_ADC_H_

#include <nireGpio.h>

extern uint8_t indizea;
extern uint8_t egoera;


#define PIN_ADC_CR1_RES 24
#define PIN_ADC_CR2_CONT 1
#define PIN_ADC_CR2_ADON 0
#define KANALA_ADC 14
#define PIN_ADC_SQR1_L 20
#define PIN_ADC_KANAL 0
#define PIN_ADC_CR2_SWSTART 30


void adcHasieratu(GPIO_TypeDef *gpio,uint8_t PIN_ADC, ADC_TypeDef* adc, uint8_t channel);
void adcClockPiztu();
void adcErresoluzioaKonfiguratu(ADC_TypeDef *adc);
void adcPiztu(ADC_TypeDef *adc);
void startConversion(ADC_TypeDef *adc);
void adcSingleConversion(ADC_TypeDef *adc, uint8_t mode);
void adcKanala(ADC_TypeDef *adc, uint8_t channel);
void ascSingleConversion(ADC_TypeDef *adc, uint8_t mode);
void adcEtenduraKonfiguratu(ADC_TypeDef *adc);
uint32_t amaieraKonprobatu(ADC_TypeDef *adc);
uint32_t adcIrakurri(ADC_TypeDef *adc);

float batezBestekoaKalkulatu(void);
void boltaiaKalkulatu(uint16_t *balioa);


#endif
