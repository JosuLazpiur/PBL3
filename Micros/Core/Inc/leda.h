#ifndef INC_LED_H_
#define INC_LED_H_

#include <nireGpio.h>


#define BERDEA 0
#define GORRIA 1
#define URDINA 2

#define PIN_LED_BERDEA 0
#define PIN_LED_GORRIA 14
#define PIN_LED_URDINA 7

#define GPIO_LED GPIOB

void ledakHasieratu(void);
void ledaPinKonfiguratu(uint8_t kolorea);
void ledaPiztu(uint8_t kolorea);
void ledaItzali(uint8_t kolorea);


#endif /* INC_LED_H_ */
