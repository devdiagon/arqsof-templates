namespace ec.edu.monster
{
    public partial class App : Application
    {
        public App()
        {
            InitializeComponent();
            // Tema oscuro fijo: no hay selector de tema (docs/CLIENT_STYLES_DOTNET.md §1)
            UserAppTheme = AppTheme.Dark;
        }

        protected override Window CreateWindow(IActivationState? activationState)
        {
            return new Window(new AppShell());
        }
    }
}
