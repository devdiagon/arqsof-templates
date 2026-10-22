using ec.edu.monster.controlador;

namespace ec.edu.monster.pruebas;

/// <summary>
/// WPF consulta CanExecute al enlazar el botón y solo vuelve a hacerlo cuando el comando avisa con CanExecuteChanged.
/// Si una pantalla carga datos (Ocupado = true) al abrirse, el botón debe habilitarse solo al terminar.
/// El molde de toda pantalla del dominio es <see cref="PantallaConCarga"/>.
/// </summary>
public class ComandosHabilitadosPruebas
{
    private class PantallaConCarga : ControladorBase
    {
        public PantallaConCarga()
        {
            Accion = new ComandoAsincrono(() => Task.CompletedTask, () => !Ocupado, this);
        }

        public ComandoAsincrono Accion { get; }

        public async Task CargarAsync()
        {
            Ocupado = true;
            await Task.Yield();
            Ocupado = false;
        }
    }

    [Fact]
    public async Task El_comando_se_habilita_al_terminar_de_cargar()
    {
        var pantalla = new PantallaConCarga();
        var estados = new List<bool>();
        pantalla.Accion.CanExecuteChanged += (_, _) => estados.Add(pantalla.Accion.CanExecute(null));

        await pantalla.CargarAsync();

        Assert.Equal([false, true], estados);
        Assert.True(pantalla.Accion.CanExecute(null));
    }

    [Fact]
    public async Task El_comando_no_se_reentra_mientras_esta_en_curso()
    {
        var liberar = new TaskCompletionSource();
        var ejecuciones = 0;
        var comando = new ComandoAsincrono(async () => { ejecuciones++; await liberar.Task; });

        var primera = comando.EjecutarAsync();
        await comando.EjecutarAsync();          // segunda llamada: ignorada
        Assert.False(comando.CanExecute(null));

        liberar.SetResult();
        await primera;

        Assert.Equal(1, ejecuciones);
        Assert.True(comando.CanExecute(null));
    }
}
