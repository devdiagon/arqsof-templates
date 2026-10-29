package ec.edu.monster.vista;

/**
 * Textos de pantalla. Los mismos valores en todos los clientes del proyecto (los define su docs/PROJECT.md);
 * aquí solo van los genéricos. Los del dominio (títulos, etiquetas, mensajes de validación) se agregan en cada proyecto.
 * El usuario ve siempre mensajes, nunca códigos.
 */
public final class Textos {

    /**
     * Arte ASCII que se muestra una sola vez al iniciar la aplicación
     */
    public static final String PORTADA = """
         _____                                                                                                      _____ 
        ( ___ )----------------------------------------------------------------------------------------------------( ___ )
         |   |                                                                                                      |   | 
         |   | $$$$$$$$\\ $$\\   $$\\ $$$$$$$\\  $$$$$$$$\\ $$\\   $$\\  $$$$$$\\  $$$$$$$\\   $$$$$$\\  $$\\   $$\\ $$\\   $$\\  |   | 
         |   | $$  _____|$$ |  $$ |$$  __$$\\ $$  _____|$$ | $$  |$$  __$$\\ $$  __$$\\ $$  __$$\\ $$$\\  $$ |$$ | $$  | |   | 
         |   | $$ |      $$ |  $$ |$$ |  $$ |$$ |      $$ |$$  / $$ /  $$ |$$ |  $$ |$$ /  $$ |$$$$\\ $$ |$$ |$$  /  |   | 
         |   | $$$$$\\    $$ |  $$ |$$$$$$$  |$$$$$\\    $$$$$  /  $$$$$$$$ |$$$$$$$\\ |$$$$$$$$ |$$ $$\\$$ |$$$$$  /   |   | 
         |   | $$  __|   $$ |  $$ |$$  __$$< $$  __|   $$  $$<   $$  __$$ |$$  __$$\\ $$  __$$ |$$ \\$$$$ |$$  $$<    |   | 
         |   | $$ |      $$ |  $$ |$$ |  $$ |$$ |      $$ |\\$$\\  $$ |  $$ |$$ |  $$ |$$ |  $$ |$$ |\\$$$ |$$ |\\$$\\   |   | 
         |   | $$$$$$$$\\ \\$$$$$$  |$$ |  $$ |$$$$$$$$\\ $$ | \\$$\\ $$ |  $$ |$$$$$$$  |$$ |  $$ |$$ | \\$$ |$$ | \\$$\\  |   | 
         |   | \\________| \\______/ \\__|  \\__|\\________|\\__|  \\__|\\__|  \\__|\\_______/ \\__|  \\__|\\__|  \\__|\\__|  \\__| |   | 
         |___|                                                                                                      |___| 
        (_____)----------------------------------------------------------------------------------------------------(_____)
            """;

    public static final String OPCION = "Opción";
    public static final String CANCELAR = "Cancelar";
    public static final String PULSE_ENTER = "Presione Enter para continuar";
    public static final String OPCION_INVALIDA = "Opción inválida";
    public static final String ERROR_INESPERADO = "Ocurrió un error inesperado, intente nuevamente";

    private Textos() {
    }
}
