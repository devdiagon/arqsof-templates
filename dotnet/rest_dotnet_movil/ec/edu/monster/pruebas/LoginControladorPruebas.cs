using ec.edu.monster.controlador;
using ec.edu.monster.excepciones;

namespace ec.edu.monster.pruebas;

public class LoginControladorPruebas
{
    private readonly AutenticadorFalso _autenticador = new();
    private readonly LoginControlador _controlador;
    private int _autenticaciones;

    public LoginControladorPruebas()
    {
        _controlador = new LoginControlador(_autenticador, () => _autenticaciones++);
    }

    private void Capturar(string usuario, string contrasenia)
    {
        _controlador.Usuario = usuario;
        _controlador.LeerContrasenia = () => contrasenia;
    }

    [Fact]
    public async Task Acceso_correcto_avisa_una_vez_y_deja_la_pantalla_libre()
    {
        Capturar("usuario", "clave");

        await _controlador.IniciarSesion.EjecutarAsync();

        Assert.Equal(1, _autenticaciones);
        Assert.Null(_controlador.Mensaje);
        Assert.False(_controlador.Ocupado);
        Assert.True(_controlador.IniciarSesion.CanExecute(null));
    }

    [Theory]
    [InlineData("", "clave")]
    [InlineData("   ", "clave")]
    [InlineData("usuario", "")]
    public async Task Campos_vacios_muestran_mensaje_y_no_llaman_al_servidor(string usuario, string contrasenia)
    {
        Capturar(usuario, contrasenia);

        await _controlador.IniciarSesion.EjecutarAsync();

        Assert.Equal("Ingrese el usuario y la contraseña.", _controlador.Mensaje);
        Assert.True(_controlador.HayMensaje);
        Assert.Empty(_autenticador.Llamadas);
        Assert.Equal(0, _autenticaciones);
    }

    [Fact]
    public async Task Error_de_negocio_muestra_solo_el_mensaje_sin_el_codigo()
    {
        _autenticador.Conducta = () => throw new ExcepcionServicio("Usuario o contraseña incorrectos", "AUTH-001");
        Capturar("usuario", "mala");

        await _controlador.IniciarSesion.EjecutarAsync();

        Assert.Equal("Usuario o contraseña incorrectos", _controlador.Mensaje);
        Assert.DoesNotContain("AUTH-001", _controlador.Mensaje);
        Assert.Equal(0, _autenticaciones);
        Assert.False(_controlador.Ocupado);
    }

    [Fact]
    public async Task Fallo_de_red_conserva_el_usuario_y_permite_reintentar()
    {
        _autenticador.Conducta = () => throw new ExcepcionServicio("No se pudo comunicar con el servidor.", "RED");
        Capturar("usuario", "clave");

        await _controlador.IniciarSesion.EjecutarAsync();

        Assert.Equal("No se pudo comunicar con el servidor.", _controlador.Mensaje);
        Assert.Equal("usuario", _controlador.Usuario);

        _autenticador.Conducta = () => Task.CompletedTask;
        await _controlador.IniciarSesion.EjecutarAsync();

        Assert.Null(_controlador.Mensaje);
        Assert.Equal(1, _autenticaciones);
    }

    [Fact]
    public async Task Error_inesperado_no_expone_detalles_internos()
    {
        _autenticador.Conducta = () => throw new InvalidOperationException("fallo interno");
        Capturar("usuario", "clave");

        await _controlador.IniciarSesion.EjecutarAsync();

        Assert.Equal("Ocurrió un error inesperado. Intente de nuevo.", _controlador.Mensaje);
        Assert.DoesNotContain("fallo interno", _controlador.Mensaje);
        Assert.False(_controlador.Ocupado);
    }

    [Fact]
    public async Task El_usuario_se_envia_recortado_y_la_contrasenia_tal_cual()
    {
        Capturar("  usuario  ", " clave ");

        await _controlador.IniciarSesion.EjecutarAsync();

        Assert.Equal(("usuario", " clave "), _autenticador.Llamadas.Single());
    }

    [Fact]
    public void El_mensaje_inicial_se_muestra_al_abrir()
    {
        var controlador = new LoginControlador(_autenticador, () => { }, "La sesión ha expirado");

        Assert.Equal("La sesión ha expirado", controlador.Mensaje);
        Assert.True(controlador.HayMensaje);
    }

    [Fact]
    public async Task Un_nuevo_intento_limpia_el_mensaje_anterior()
    {
        Capturar("", "");
        await _controlador.IniciarSesion.EjecutarAsync();
        Assert.True(_controlador.HayMensaje);

        Capturar("usuario", "clave");
        await _controlador.IniciarSesion.EjecutarAsync();

        Assert.False(_controlador.HayMensaje);
    }
}
