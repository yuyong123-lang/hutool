package cn.hutool.http.client;

import cn.hutool.core.lang.Console;
import cn.hutool.http.client.engine.httpclient4.HttpClient4Engine;
import cn.hutool.http.meta.Method;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class HttpClient4EngineTest {

	@SuppressWarnings("resource")
	@Test
	@Disabled
	public void getTest() {
		final ClientEngine engine = new HttpClient4Engine();

		final Request req = Request.of("https://www.hutool.cn/").method(Method.GET);
		final Response res = engine.send(req);

		Console.log(res.getStatus());
		Console.log(res.body());
	}
}