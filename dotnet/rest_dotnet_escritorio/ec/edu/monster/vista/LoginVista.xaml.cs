using System.Windows.Controls;
using ec.edu.monster.controlador;

namespace ec.edu.monster.vista;

/// <summary>Solo conecta el PasswordBox con el controlador y da el foco inicial; sin lógica de negocio.</summary>
public partial class LoginVista : UserControl
{
    public LoginVista()
    {
        InitializeComponent();
        Loaded += (_, _) =>
        {
            if (DataContext is LoginControlador controlador)
                controlador.LeerContrasenia = () => Contrasenia.Password;
            Usuario.Focus();
        };
    }
}
