/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.elasticsearch7.internal.sidecar;

import com.liferay.petra.string.StringBundler;

import java.util.Arrays;
import java.util.List;

/**
 * @author Bryan Engler
 */
public class ElasticsearchDistribution implements Distribution {

	public static final String VERSION = "7.17.29";

	@Override
	public Distributable getElasticsearchDistributable() {
		return new DistributableImpl(
			StringBundler.concat(
				"https://artifacts.elastic.co/downloads/elasticsearch",
				"/elasticsearch-", VERSION, "-no-jdk-linux-x86_64.tar.gz"),
			_ELASTICSEARCH_CHECKSUM);
	}

	@Override
	public List<Distributable> getPluginDistributables() {
		return Arrays.asList(
			new DistributableImpl(
				_getDownloadURLString("analysis-icu"), _ICU_CHECKSUM),
			new DistributableImpl(
				_getDownloadURLString("analysis-kuromoji"), _KUROMOJI_CHECKSUM),
			new DistributableImpl(
				_getDownloadURLString("analysis-smartcn"), _SMARTCN_CHECKSUM),
			new DistributableImpl(
				_getDownloadURLString("analysis-stempel"), _STEMPEL_CHECKSUM));
	}

	private String _getDownloadURLString(String plugin) {
		return StringBundler.concat(
			"https://artifacts.elastic.co/downloads/elasticsearch-plugins/",
			plugin, "/", plugin, "-", VERSION, ".zip");
	}

	private static final String _ELASTICSEARCH_CHECKSUM =
		"4ad566436ca792455c5afbdfd46908da3176d37f8ff427122bbb6ae23b7badb614e3" +
			"126266cbbfc266a832f6d20126626c331ca318394bcc7285d8009ecc5843";

	private static final String _ICU_CHECKSUM =
		"0fd03cdfda357d2358293801be04722df9650810cf1a7243ae57cb419ba5df0c2b4f" +
			"5e84c8c368d373a9b40b768cc729e9fce14ecb3fe86b5426d470dab0233c";

	private static final String _KUROMOJI_CHECKSUM =
		"eda3f4d1a9eaae39d11694ebc88fe4b252c3966da961ef8d0bb3a4d99385e8710c28" +
			"ec677b078a36991a37c2359e50859506c5d888de898079c15146c4035536";

	private static final String _SMARTCN_CHECKSUM =
		"c80991543cc46e31b7dd10aafe135588c97c53c1ff109e6a7f83179d27d381a180ac" +
			"076412270b52fed1270602d1da24a3b79aaf229fe5288f05c38e4b57d0e6";

	private static final String _STEMPEL_CHECKSUM =
		"febecb297eb7ce918a7921a8624ab2113de806a949c632d26abe154703460d80395f" +
			"7219df0812af68830eb39d6c01ef635ee65d6919aadbaf29788fef67f9b5";

}