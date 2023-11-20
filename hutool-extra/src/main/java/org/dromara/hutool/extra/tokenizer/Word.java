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

package org.dromara.hutool.extra.tokenizer;

import java.io.Serializable;

/**
 * 表示分词中的一个词
 *
 * @author looly
 *
 */
public interface Word extends Serializable {

	/**
	 * 获取单词文本
	 *
	 * @return 单词文本
	 */
	String getText();

	/**
	 * 获取本词的起始位置
	 *
	 * @return 起始位置
	 */
	int getStartOffset();

	/**
	 * 获取本词的结束位置
	 *
	 * @return 结束位置
	 */
	int getEndOffset();
}