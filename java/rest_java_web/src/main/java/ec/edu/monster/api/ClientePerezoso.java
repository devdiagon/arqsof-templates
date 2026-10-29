package ec.edu.monster.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

/**
 * Cliente de un servicio que se crea la primera vez que se usa. En la web el WAR se despliega aunque el servidor REST
 * esté apagado y los servlets atienden varias peticiones a la vez: el cliente es <b>uno solo por servicio
 * para toda la aplicación</b> (el {@code HttpClient} admite llamadas concurrentes) y, si su creación falla, se reintenta en
 * la siguiente llamada en lugar de dejar la aplicación inutilizable.
 *
 * <pre>
 * XCliente cliente = ClientePerezoso.de(XCliente.class,
 *         XClienteApi::crear);
 * </pre>
 *
 * Las excepciones del cliente llegan tal cual, sin envolver. La interfaz del cliente debe ser <b>pública</b>: con una interfaz
 * de paquete, la invocación reflexiva falla al llamarse desde otro paquete.
 */
public final class ClientePerezoso {

    private ClientePerezoso() {
    }

    public static <C> C de(Class<C> interfaz, Supplier<C> fabrica) {
        return interfaz.cast(Proxy.newProxyInstance(interfaz.getClassLoader(), new Class<?>[]{interfaz},
                new Perezoso<>(fabrica)));
    }

    private static final class Perezoso<C> implements InvocationHandler {

        private final Supplier<C> fabrica;
        private volatile C destino;

        Perezoso(Supplier<C> fabrica) {
            this.fabrica = fabrica;
        }

        @Override
        public Object invoke(Object proxy, Method metodo, Object[] args) throws Throwable {
            try {
                return metodo.invoke(destino(), args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private C destino() {
            C actual = destino;
            if (actual == null) {
                synchronized (this) {
                    actual = destino;
                    if (actual == null) {
                        actual = fabrica.get();
                        destino = actual;
                    }
                }
            }
            return actual;
        }
    }
}
