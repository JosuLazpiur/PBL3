/*
 * timer.h
 *
 *  Created on: Nov 18, 2024
 *      Author: eneko
 */

#ifndef INC_TIMER_H_
#define INC_TIMER_H_

#include <nireGpio.h>
#include <leda.h>
#include <adc.h>

extern uint8_t led_egoera;

void timerHasieratu(uint16_t max_ms);
void timerEtendura(void);


#endif /* INC_TIMER_H_ */
