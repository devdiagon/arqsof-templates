using System.Globalization;
using System.Windows.Data;

namespace ec.edu.monster.vista;

/// <summary>Invierte un booleano (p. ej. habilitar la pantalla mientras NO está ocupada).</summary>
public class NegacionConvertidor : IValueConverter
{
    public object Convert(object value, Type targetType, object parameter, CultureInfo culture) =>
        value is not true;

    public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) =>
        value is not true;
}
