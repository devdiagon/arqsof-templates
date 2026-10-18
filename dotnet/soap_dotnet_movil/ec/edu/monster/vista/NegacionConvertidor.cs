using System.Globalization;

namespace ec.edu.monster.vista;

/// <summary>Invierte un booleano: habilita los controles mientras el controlador no está ocupado.</summary>
public class NegacionConvertidor : IValueConverter
{
    public object? Convert(object? value, Type targetType, object? parameter, CultureInfo culture) =>
        value is bool b ? !b : value;

    public object? ConvertBack(object? value, Type targetType, object? parameter, CultureInfo culture) =>
        value is bool b ? !b : value;
}
