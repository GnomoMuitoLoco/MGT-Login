package br.com.magnatasoriginal.mgtlogin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logger centralizado para o mod MGT-Login.
 * Substitui chamadas diretas a System.out/err em todo o projeto.
 */
public class ModLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("MGT-Login");

    public static void info(String mensagem) {
        LOGGER.info(mensagem);
    }

    public static void aviso(String mensagem) {
        LOGGER.warn(mensagem);
    }

    public static void erro(String mensagem) {
        LOGGER.error(mensagem);
    }

    public static void erro(String mensagem, Throwable throwable) {
        LOGGER.error(mensagem, throwable);
    }

    public static void debug(String mensagem) {
        LOGGER.debug(mensagem);
    }
}

