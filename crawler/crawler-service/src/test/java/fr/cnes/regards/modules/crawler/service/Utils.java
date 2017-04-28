package fr.cnes.regards.modules.crawler.service;

public final class Utils {

    public interface ConsumerWithException<T> {

        void accept(T t) throws Exception;
    }

    public static <T> void execute(ConsumerWithException<T> consumer, T arg) {
        try {
            consumer.accept(arg);
        } catch (Throwable t) {
            //            t.printStackTrace();
        }
    }
}
