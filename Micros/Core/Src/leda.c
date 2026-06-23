#include <leda.h>

void ledakHasieratu(void) {
	gpioClockPIztu(GPIO_LED);

	ledaPinKonfiguratu(BERDEA);
	ledaPinKonfiguratu(GORRIA);
	ledaPinKonfiguratu(URDINA);
}

void ledaPinKonfiguratu(uint8_t kolorea) {
	switch (kolorea) {
		case BERDEA:
			gpioPinKonfiguratu(GPIO_LED, PIN_LED_BERDEA, OUT);
			break;
		case GORRIA:
			gpioPinKonfiguratu(GPIO_LED, PIN_LED_GORRIA, OUT);
			break;
		case URDINA:
			gpioPinKonfiguratu(GPIO_LED, PIN_LED_URDINA, OUT);
			break;
		default:
			// Akziorik ez
			break;
	}
}


void ledaPiztu(uint8_t kolorea) {

	switch (kolorea) {
		case BERDEA:
			gpio_Piztu(GPIO_LED, PIN_LED_BERDEA);
			break;
		case GORRIA:
			gpio_Piztu(GPIO_LED, PIN_LED_GORRIA);
			break;
		case URDINA:
			gpio_Piztu(GPIO_LED, PIN_LED_URDINA);
			break;
		default:
			// Akziorik ez
			break;
	}
}

void ledaItzali(uint8_t kolorea) {
	switch (kolorea) {
		case BERDEA:
			gpio_Itzali(GPIO_LED, PIN_LED_BERDEA);
			break;
		case GORRIA:
			gpio_Itzali(GPIO_LED, PIN_LED_GORRIA);
			break;
		case URDINA:
			gpio_Itzali(GPIO_LED, PIN_LED_URDINA);
			break;
		default:
			// Akziorik ez
			break;
	}
}
