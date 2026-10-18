using ec.edu.monster.excepciones;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

public class ServicioBasePruebas
{
    // Servicio mínimo que expone los miembros protegidos de ServicioBase.
    private class ServicioEjemplo : ServicioBase
    {
        public ServicioEjemplo(Sesion sesion) : base(sesion) { }
        public string Token() => ExigirToken();
        public void Comprobar(string? estado, string? codigo, string? mensaje) => Verificar(estado, codigo, mensaje);
        public object Presente(object? respuesta) => ExigirRespuesta(respuesta);
    }

    private readonly Sesion _sesion = new();
    private readonly ServicioEjemplo _servicio;

    public ServicioBasePruebas()
    {
        _servicio = new ServicioEjemplo(_sesion);
    }

    [Fact]
    public void Sin_token_se_exige_login_sin_llamar_al_servidor()
    {
        var ex = Assert.Throws<ExcepcionServicio>(_servicio.Token);

        Assert.Equal("AUTH-002", ex.Codigo);
        Assert.True(ex.ExigeLogin);
    }

    [Fact]
    public void Con_token_se_devuelve_el_de_la_sesion()
    {
        _sesion.Token = "ABC";

        Assert.Equal("ABC", _servicio.Token());
        Assert.True(_servicio.HaySesion);
    }

    [Theory]
    [InlineData("OK")]
    [InlineData("ok")]
    public void Estado_OK_no_lanza(string estado)
    {
        _servicio.Comprobar(estado, "200", "Correcto");
    }

    [Fact]
    public void Error_de_negocio_lanza_con_codigo_y_mensaje_del_servidor_y_conserva_la_sesion()
    {
        _sesion.Token = "ABC";

        var ex = Assert.Throws<ExcepcionServicio>(() => _servicio.Comprobar("ERROR", "DATA-001", "El valor debe ser numérico"));

        Assert.Equal("DATA-001", ex.Codigo);
        Assert.Equal("El valor debe ser numérico", ex.Message);
        Assert.False(ex.ServidorNoDisponible);
        Assert.True(_servicio.HaySesion);
    }

    [Theory]
    [InlineData("AUTH-002")]
    [InlineData("AUTH-003")]
    public void Token_invalido_o_vencido_descarta_la_sesion(string codigo)
    {
        _sesion.Token = "ABC";

        var ex = Assert.Throws<ExcepcionServicio>(() => _servicio.Comprobar("ERROR", codigo, "Sesión expirada"));

        Assert.True(ex.ExigeLogin);
        Assert.False(_servicio.HaySesion);
    }

    [Fact]
    public void Campos_ausentes_en_un_error_no_rompen()
    {
        var ex = Assert.Throws<ExcepcionServicio>(() => _servicio.Comprobar(null, null, null));

        Assert.Equal("ERR-500", ex.Codigo);
    }

    [Fact]
    public void Respuesta_nula_lanza_error()
    {
        Assert.Throws<ExcepcionServicio>(() => _servicio.Presente(null));
    }

    [Theory]
    [InlineData("1,5", "1.5")]
    [InlineData(" 2.25 ", "2.25")]
    [InlineData("-3,0", "-3.0")]
    public void El_valor_decimal_se_envia_con_punto(string escrito, string enviado)
    {
        Assert.Equal(enviado, ServicioBase.NormalizarValor(escrito));
    }

    [Fact]
    public void Cerrar_sesion_descarta_el_token()
    {
        _sesion.Token = "ABC";

        _servicio.CerrarSesion();

        Assert.False(_servicio.HaySesion);
    }
}
