package redis.clients.jedis.tests.benchmark;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class PoolBenchmark {
    private static final int TOTAL_OPERATIONS = 100000;

    public static void main(String[] args) throws UnknownHostException,
	    IOException, TimeoutException, InterruptedException {
	Jedis j = new Jedis("localhost");
	j.connect();
	j.auth("foobared");
	j.flushAll();
	j.quit();
	j.disconnect();
	long t = System.currentTimeMillis();
	// withoutPool();
	withPool();
	long elapsed = System.currentTimeMillis() - t;
	System.out.println(((1000 * 3 * TOTAL_OPERATIONS) / elapsed) + " ops");
    }

    private static void withoutPool() throws InterruptedException {
	List<Thread> tds = new ArrayList<Thread>();

	for (int i = 0; i < TOTAL_OPERATIONS; i++) {
	    final String key = "foo" + i;
	    Thread hj = new Thread(new Runnable() {
		@Override
		public void run() {
		    Jedis j = new Jedis("localhost");
		    try {
			j.connect();
			j.auth("foobared");
			j.set(key, key);
			j.get(key);
			j.quit();
			j.disconnect();
		    } catch (UnknownHostException e) {
			e.printStackTrace();
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}
	    });
	    tds.add(hj);
	    hj.start();
	}
	for (Thread thread : tds) {
	    thread.join();
	}
    }

    private static void withPool() throws InterruptedException {
	final JedisPool pool = new JedisPool("localhost");
	pool.setResourcesNumber(1000);
	pool.setDefaultPoolWait(20);
	pool.init();
	List<Thread> tds = new ArrayList<Thread>();

	for (int i = 0; i < TOTAL_OPERATIONS; i++) {
	    final String key = "foo" + i;
	    Thread hj = new Thread(new Runnable() {
		@Override
		public void run() {
		    try {
			Jedis j = pool.getResource();
			j.auth("foobared");
			j.set(key, key);
			j.get(key);
			pool.returnResource(j);
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    });
	    tds.add(hj);
	    hj.start();
	}
	pool.destroy();
    }
}