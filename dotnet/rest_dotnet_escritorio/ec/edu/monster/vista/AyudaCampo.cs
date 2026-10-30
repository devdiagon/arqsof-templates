using System.Windows;

namespace ec.edu.monster.vista;

/// <summary>
/// Propiedad adjunta para marcar un campo como inválido (borde de 2 en <c>Error</c>, CLIENT_STYLES_DOTNET.md §5.3).
/// Los controladores la fijan al nombrar el primer campo vacío; los estilos de <c>estilos/controles.xaml</c> la leen.
/// </summary>
public static class AyudaCampo
{
    public static readonly DependencyProperty InvalidoProperty = DependencyProperty.RegisterAttached(
        "Invalido", typeof(bool), typeof(AyudaCampo), new PropertyMetadata(false));

    public static bool GetInvalido(DependencyObject objeto) => (bool)objeto.GetValue(InvalidoProperty);

    public static void SetInvalido(DependencyObject objeto, bool valor) => objeto.SetValue(InvalidoProperty, valor);
}
