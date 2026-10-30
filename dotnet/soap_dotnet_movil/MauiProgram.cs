using Microsoft.Extensions.Logging;
using Microsoft.Maui.Handlers;

namespace ec.edu.monster
{
    public static class MauiProgram
    {
        public static MauiApp CreateMauiApp()
        {
            var builder = MauiApp.CreateBuilder();
            // Sin fuentes incrustadas: se usa la del sistema (docs/CLIENT_STYLES_DOTNET.md §3).
            builder.UseMauiApp<App>();

            // El Entry va dentro de un Border de radio 8 (CampoTexto): se quita su subrayado de Android.
            EntryHandler.Mapper.AppendToMapping("SinSubrayado", (handler, _) =>
            {
#if ANDROID
                handler.PlatformView.BackgroundTintList =
                    Android.Content.Res.ColorStateList.ValueOf(Android.Graphics.Color.Transparent);
#endif
            });

#if DEBUG
            builder.Logging.AddDebug();
#endif

            return builder.Build();
        }
    }
}
