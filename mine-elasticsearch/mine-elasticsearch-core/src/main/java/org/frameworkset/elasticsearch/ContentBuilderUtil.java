package org.frameworkset.elasticsearch;

import com.fasterxml.jackson.core.JsonParseException;
import org.elasticsearch.common.xcontent.*;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author he.wl
 * @version 1.0.0
 * @className org.frameworkset.elasticsearch.ContentBuilderUtil
 * @description 构建工具 -- 使用ElasticSearch的实用方法{@link org.elasticsearch.common.xcontent.XContentBuilder}
 * @date 2020/8/20 15:17
 */
public class ContentBuilderUtil {

	private final static Charset charset = Charset.forName("UTF-8");

	protected ContentBuilderUtil() {}

	/**
	 * 拼接字段
	 * @param builder
	 * @param fieldName
	 * @param data
	 * @throws IOException
	 */
	public static void appendField(XContentBuilder builder, String fieldName, byte[] data) throws IOException {
		// TODO 方法已废除, 未来需要替换
		XContentType contentType = XContentFactory.xContentType(data);
		if(null != contentType) {
			addComplexField(builder, fieldName, contentType, data);
		} else {
			addSimpleField(builder, fieldName, data);
		}
	}

	/**
	 * 添加简单字段
	 * @param builder       用于构建XContent(即json)的工具
	 * @param fieldName     字段名
	 * @param data          值
	 * @throws IOException
	 */
	public static void addSimpleField(XContentBuilder builder, String fieldName,
	                                  byte[] data) throws IOException {
		builder.field(fieldName, new String(data, charset));
	}

	/**
	 * 添加复合字段
	 * @param builder
	 * @param fieldName
	 * @param contentType   内容的类型, 枚举
	 * @param data
	 * @throws IOException
	 */
	public static void addComplexField(XContentBuilder builder, String fieldName,
	                                   XContentType contentType, byte[] data) throws IOException {
		// XContent 解析器, 受JSON和pull解析的启发，在处理内容之上的一个通用抽象
		XContentParser parser = null;
		try {
			/**
			 * Elasticsearch将直接接受JSON，但我们需要验证传入的事件是JSON首先。遗憾的是，elasticsearch JSON解析器是一个流解析器，
			 * 因此我们需要实例化它，解析事件以验证它，然后再次实例化它以向elasticsearch提供JSON。如果验证失败，
			 * 那么传入的事件将作为纯文本提交给elasticsearch。
			 */
			parser = XContentFactory.xContent(contentType).createParser(NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, data);
			while (null != parser.nextToken()) {}

			// 如果JSON是有效的，那么包含它
			try {
				parser = XContentFactory.xContent(contentType).createParser(NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, data);
				// 添加字段名，但不添加值
				builder.field(fieldName);
				// 这将添加整个解析后的内容作为字段的值
				builder.copyCurrentStructure(parser);
			} finally {
				if(null != parser) {
					parser.close();
				}
			}
		} catch(JsonParseException ex) {
			// 如果这里出现异常，最有可能的原因是嵌套的JSON，在正文中搞不清楚。这时只要按原样推过去就行了
			addSimpleField(builder, fieldName, data);
		} finally {
			if(null != parser) {
				parser.close();
			}
		}
	}
}
