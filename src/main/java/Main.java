import java.util.*;

public class Main {

    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args) {
        final int n = 1000;

        Thread threadLog = new Thread(() -> {
            int count = 0;
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (sizeToFreq) {
                    try {
                        sizeToFreq.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    int keyMaxFreq = findKeyMaxFreq(sizeToFreq);
                    System.out.println(String.format("%d: Текущий лидер %d с частотой %d", count++, keyMaxFreq, sizeToFreq.get(keyMaxFreq)));

                }
            }
            System.out.println("Логирующий поток завершил работу");
        });
        threadLog.start();

        List<Thread> threads = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            threads.add(new Thread(() -> {
                String path = generateRoute("RLRFR", n);
                int count = calcLetter(path, 'R');
                synchronized (sizeToFreq) {
                    if (sizeToFreq.containsKey(count)) {
                        sizeToFreq.put(count, sizeToFreq.get(count) + 1);
                    } else {
                        sizeToFreq.put(count, 1);
                    }
                    sizeToFreq.notifyAll();

                }
                //System.out.println(path + " -> " + count);
            }));
            threads.get(i).start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        threadLog.interrupt();

        int key = findKeyMaxFreq(sizeToFreq);

        int max = sizeToFreq.remove(key);
        System.out.println(String.format("Самое частое количество повторений %d (встретилось %d раз)", key, max));
        System.out.println("Другие размеры:");
        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
            System.out.println(String.format("- %d (%d раз)", entry.getKey(), entry.getValue()));
        }
    }

    public static int findKeyMaxFreq(Map<Integer, Integer> map) {
        int key = -1;
        int max = Integer.MIN_VALUE;
        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                key = entry.getKey();
            }
        }
        return key;
    }

    public static int calcLetter(String str, char letter) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == letter) {
                count++;
            }
        }
        return count;
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}
