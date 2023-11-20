/*
 * Copyright (c) 2023 looly(loolly@aliyun.com)
 * Hutool is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          https://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.dromara.hutool.db;

import org.dromara.hutool.core.lang.Console;
import org.dromara.hutool.db.sql.Query;
import org.dromara.hutool.db.sql.SqlBuilder;
import org.dromara.hutool.db.sql.SqlUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Oracle操作单元测试
 *
 * @author looly
 *
 */
public class OracleTest {

	@Test
	public void oraclePageSqlTest() {
		final Page page = new Page(0, 10);
		final Entity where = Entity.of("PMCPERFORMANCEINFO").set("yearPI", "2017");
		final Query query = new Query(SqlUtil.buildConditions(where), where.getTableName());
		query.setPage(page);

		final SqlBuilder find = SqlBuilder.of().query(query).orderBy(page.getOrders());
		final int[] startEnd = page.getStartEnd();
		final SqlBuilder builder = SqlBuilder.of().append("SELECT * FROM ( SELECT row_.*, rownum rownum_ from ( ")//
				.append(find)//
				.append(" ) row_ where rownum <= ").append(startEnd[1])//
				.append(") table_alias")//
				.append(" where table_alias.rownum_ >= ").append(startEnd[0]);//

		final String ok = "SELECT * FROM "//
				+ "( SELECT row_.*, rownum rownum_ from ( SELECT * FROM PMCPERFORMANCEINFO WHERE yearPI = ? ) row_ "//
				+ "where rownum <= 10) table_alias where table_alias.rownum_ >= 0";//
		Assertions.assertEquals(ok, builder.toString());
	}

	@Test
	@Disabled
	public void insertTest() {
		for (int id = 100; id < 200; id++) {
			Db.of("orcl").insert(Entity.of("T_USER")//
					.set("ID", id)//
					.set("name", "测试用户" + id)//
					.set("TEXT", "描述" + id)//
					.set("TEST1", "t" + id)//
			);
		}
	}

	@Test
	@Disabled
	public void pageTest() {
		final PageResult<Entity> result = Db.of("orcl").page(Entity.of("T_USER"), new Page(2, 10));
		for (final Entity entity : result) {
			Console.log(entity.get("ID"));
		}
	}

}