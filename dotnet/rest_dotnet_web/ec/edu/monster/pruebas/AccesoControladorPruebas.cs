using ec.edu.monster.controlador;
using ec.edu.monster.excepciones;
using ec.edu.monster.modelo;

namespace ec.edu.monster.pruebas;

public class AccesoControladorPruebas
{
    private readonly AutenticadorFalso _autenticador = new();
    private readonly Sesion _sesion = new();
    private readonly AccesoControlador _controlador;

    public AccesoControladorPruebas()
    {
        _controlador = new AccesoControlador(_autenticador, _sesion);
    }

    private static AccesoModelo Modelo(string usuario, string contrasenia) =>
        new() { Usuario = usuario, Contrasenia = contrasenia };

    [Fact]
    public async Task Acceso_correcto_redirige_a_inicio()
    {
        var resultado = await _controlador.Acceder(Modelo(" usuario-de-prueba ", "clave-de-prueba"));

        var redireccion = Assert.IsType<RedirectToActionResult>(resultado);
        Assert.Equal("Inicio", redireccion.ControllerName);
        Assert.Equal(("usuario-de-prueba", "clave-de-prueba"), Assert.Single(_autenticador.Llamadas));
    }

    [Theory]
    [InlineData("", "clave")]
    [InlineData("usuario", "")]
    [InlineData("   ", "clave")]
    public async Task Campos_vacios_no_llaman_al_servidor(string usuario, string contrasenia)
    {
        var resultado = await _controlador.Acceder(Modelo(usuario, contrasenia));

        var modelo = Assert.IsType<AccesoModelo>(Assert.IsType<ViewResult>(resultado).Model);
        Assert.Equal("Ingrese el usuario y la contraseña.", modelo.Mensaje);
        Assert.Empty(_autenticador.Llamadas);
    }

    [Fact]
    public async Task Error_de_negocio_muestra_solo_el_mensaje_y_conserva_el_usuario()
    {
        _autenticador.Conducta = () => throw new ExcepcionServicio("Usuario o contraseña incorrectos", "AUTH-001");

        var resultado = await _controlador.Acceder(Modelo("usuario-de-prueba", "mala"));

        var modelo = Assert.IsType<AccesoModelo>(Assert.IsType<ViewResult>(resultado).Model);
        Assert.Equal("Usuario o contraseña incorrectos", modelo.Mensaje);
        Assert.Equal("usuario-de-prueba", modelo.Usuario);
        Assert.Equal(string.Empty, modelo.Contrasenia);
    }

    [Fact]
    public async Task Fallo_de_red_muestra_el_mensaje_del_cliente()
    {
        _autenticador.Conducta = () => throw new ExcepcionServicio("No se pudo comunicar con el servidor.", ExcepcionServicio.CodigoRed);

        var resultado = await _controlador.Acceder(Modelo("usuario-de-prueba", "clave-de-prueba"));

        var modelo = Assert.IsType<AccesoModelo>(Assert.IsType<ViewResult>(resultado).Model);
        Assert.Equal("No se pudo comunicar con el servidor.", modelo.Mensaje);
    }

    [Fact]
    public async Task Excepcion_inesperada_muestra_un_mensaje_generico_sin_detalles()
    {
        _autenticador.Conducta = () => throw new InvalidOperationException("detalle interno");

        var resultado = await _controlador.Acceder(Modelo("usuario-de-prueba", "clave-de-prueba"));

        var modelo = Assert.IsType<AccesoModelo>(Assert.IsType<ViewResult>(resultado).Model);
        Assert.Equal("Ocurrió un error inesperado. Intente de nuevo.", modelo.Mensaje);
    }

    [Fact]
    public void Cerrar_sesion_descarta_el_token_y_vuelve_al_acceso()
    {
        _sesion.Token = "ABC";

        var resultado = _controlador.CerrarSesion();

        Assert.Null(_sesion.Token);
        Assert.Equal("Acceder", Assert.IsType<RedirectToActionResult>(resultado).ActionName);
    }
}
