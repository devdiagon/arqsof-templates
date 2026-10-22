using ec.edu.monster.controlador;

namespace ec.edu.monster.pruebas;

public class NavegadorControladorPruebas
{
    private class PantallaFalsa : ControladorBase { }

    private readonly NavegadorControlador _navegador = new();

    [Fact]
    public void Al_inicio_no_hay_pantalla()
    {
        Assert.Null(_navegador.Actual);
    }

    [Fact]
    public void Mostrar_cambia_la_pantalla_actual_y_avisa_a_la_vista()
    {
        var avisos = new List<string?>();
        _navegador.PropertyChanged += (_, e) => avisos.Add(e.PropertyName);
        var pantalla = new PantallaFalsa();

        _navegador.Mostrar(pantalla);

        Assert.Same(pantalla, _navegador.Actual);
        Assert.Contains(nameof(NavegadorControlador.Actual), avisos);
    }

    [Fact]
    public void Mostrar_otra_pantalla_reemplaza_la_anterior()
    {
        _navegador.Mostrar(new PantallaFalsa());
        var segunda = new PantallaFalsa();

        _navegador.Mostrar(segunda);

        Assert.Same(segunda, _navegador.Actual);
    }
}
