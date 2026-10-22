using ec.edu.monster.vista;

namespace ec.edu.monster
{
    public partial class AppShell : Shell
    {
        public AppShell()
        {
            InitializeComponent();

            // DOMINIO: una ruta por pantalla del dominio (la ruta «login» es el ShellContent de AppShell.xaml)
            Routing.RegisterRoute(Navegacion.RutaInicio, typeof(InicioVista));
        }
    }
}
