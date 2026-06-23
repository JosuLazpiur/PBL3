#include <timer.h>

uint8_t led_egoera = 0;

// Configura el temporizador con un prescaler y un valor ARR específicos
void timerConfigurar(uint16_t max_ms) {
    RCC->APB2ENR |= 1 << 1;  // Habilitar el reloj para TIM8

    TIM8->PSC = 8000 - 1;    // Prescaler (1 ms por incremento)
    TIM8->ARR = max_ms - 1;  // Valor de recarga automática (Auto-Reload)
    TIM8->CNT = 0;           // Reiniciar el contador

    TIM8->CR1 |= (1 << 2);   // URS: Actualizaciones solo en desbordamiento
    TIM8->EGR |= (1 << 0);   // Generar un evento de actualización (UG) para inicializar registros
    TIM8->SR &= ~(1 << 0);   // Asegurar que UIF esté en 0 antes de empezar
}

// Inicia el temporizador
void timerStart(void) {
    TIM8->CR1 |= (1 << 0);   // CEN: Habilitar el temporizador
}

// Detiene el temporizador
void timerStop(void) {
    TIM8->CR1 &= ~(1 << 0);  // CEN: Detener el temporizador
    TIM8->CNT = 0;           // Reiniciar el contador
}

// Comprueba si ocurrió un desbordamiento
uint32_t TimerOverFlow(void) {
    TIM8->SR &= ~((1 << 1) | (1 << 2) | (1 << 3) | (1 << 4));  // Limpiar CCxIF
    if (TIM8->SR & (1 << 0)) {  // Verificar si UIF está activado
        TIM8->SR &= ~(1 << 0);  // Limpiar UIF escribiendo 0
        return 1;               // Retornar que hubo un desbordamiento
    }
    return 0;                   // No hubo desbordamiento
}
