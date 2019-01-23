/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.apio.architect.internal.wiring.osgi.manager.cache;

import static javax.ws.rs.core.Variant.VariantListBuilder.newInstance;

import com.liferay.apio.architect.documentation.contributor.CustomDocumentation;
import com.liferay.apio.architect.identifier.Identifier;
import com.liferay.apio.architect.internal.action.ActionSemantics;
import com.liferay.apio.architect.internal.annotation.representor.processor.ParsedType;
import com.liferay.apio.architect.internal.message.json.BatchResultMessageMapper;
import com.liferay.apio.architect.internal.message.json.DocumentationMessageMapper;
import com.liferay.apio.architect.internal.message.json.EntryPointMessageMapper;
import com.liferay.apio.architect.internal.message.json.ErrorMessageMapper;
import com.liferay.apio.architect.internal.message.json.PageMessageMapper;
import com.liferay.apio.architect.internal.message.json.SingleModelMessageMapper;
import com.liferay.apio.architect.internal.unsafe.Unsafe;
import com.liferay.apio.architect.internal.wiring.osgi.alias.EmptyFunction;
import com.liferay.apio.architect.representor.Representor;
import com.liferay.apio.architect.routes.CollectionRoutes;
import com.liferay.apio.architect.routes.ItemRoutes;
import com.liferay.apio.architect.routes.NestedCollectionRoutes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;

/**
 * Acts as a central cache for most managers.
 *
 * <p>
 * There should only be one instance of this class, accessible through {@link
 * #INSTANCE}.
 * </p>
 *
 * @author Alejandro Hernández
 */
public class ManagerCache {

	/**
	 * The {@link ManagerCache} instance.
	 */
	public static final ManagerCache INSTANCE = new ManagerCache();

	/**
	 * Adds action semantics.
	 *
	 * @param actionSemantics the action semantics
	 */
	public void addActionSemantics(ActionSemantics actionSemantics) {
		if (_actionSemantics == null) {
			_actionSemantics = new ArrayList<>();
		}

		_actionSemantics.add(actionSemantics);
	}

	/**
	 * Clears the cache.
	 */
	public void clear() {
		_actionSemantics = null;
		_collectionRoutes = null;
		_documentationMessageMappers = null;
		_entryPointMessageMappers = null;
		_errorMessageMappers = null;
		_identifierClasses = null;
		_reusableIdentifierClasses = null;
		_itemRoutes = null;
		_names = null;
		_nestedCollectionRoutes = null;
		_pageMessageMappers = null;
		_parsedTypes = null;
		_batchResultMessageMappers = null;
		_representors = null;
		_reusableNestedCollectionRoutes = null;
		_rootResourceNameSdks = null;
		_singleModelMessageMappers = null;
	}

	public List<ActionSemantics> getActionSemantics(
		EmptyFunction computeEmptyFunction) {

		if (_actionSemantics == null) {
			computeEmptyFunction.invoke();
		}

		return _actionSemantics;
	}

	/**
	 * Returns the batch result message mapper, if present, for the current
	 * request; {@code Optional#empty()} otherwise.
	 *
	 * @param  request the current request
	 * @param  computeEmptyFunction the function that can be called to compute
	 *         the data
	 * @return the batch result message mapper, if present; {@code
	 *         Optional#empty()} otherwise
	 */
	public <T> Optional<BatchResultMessageMapper<T>>
		getBatchResultMessageMapperOptional(
			Request request, EmptyFunction computeEmptyFunction) {

		if (_batchResultMessageMappers == null) {
			computeEmptyFunction.invoke();
		}

		Optional<BatchResultMessageMapper> optional = _getMessageMapperOptional(
			request, _batchResultMessageMappers);

		return optional.map(Unsafe::unsafeCast);
	}

	public Map<String, CollectionRoutes> getCollectionRoutes(
		EmptyFunction computeEmptyFunction) {

		if (_collectionRoutes == null) {
			computeEmptyFunction.invoke();
		}

		return _collectionRoutes;
	}

	public CustomDocumentation getDocumentationContribution(
		EmptyFunction computeEmptyFunction) {

		if (_customDocumentation == null) {
			computeEmptyFunction.invoke();
		}

		return _customDocumentation;
	}

	/**
	 * Returns the documentation message mapper, if present, for the current
	 * request; {@code Optional#empty()} otherwise.
	 *
	 * @param  request the current request
	 * @param  computeEmptyFunction the function that can be called to compute
	 *         the data
	 * @return the documentation message mapper, if present; {@code
	 *         Optional#empty()} otherwise
	 */
	public Optional<DocumentationMessageMapper>
		getDocumentationMessageMapperOptional(
			Request request, EmptyFunction computeEmptyFunction) {

		if (_documentationMessageMappers == null) {
			computeEmptyFunction.invoke();
		}

		Optional<DocumentationMessageMapper> optional =
			_getMessageMapperOptional(request, _documentationMessageMappers);

		return optional.map(Unsafe::unsafeCast);
	}

	/**
	 * Returns the entry point message mapper, if present, for the current
	 * request; {@code Optional#empty()} otherwise.
	 *
	 * @param  request the current request
	 * @param  computeEmptyFunction the function that can be called to compute
	 *         the data
	 * @return the entry point message mapper, if present; {@code
	 *         Optional#empty()} otherwise
	 */
	public Optional<EntryPointMessageMapper> getEntryPointMessageMapperOptional(
		Request request, EmptyFunction computeEmptyFunction) {

		if (_entryPointMessageMappers == null) {
			computeEmptyFunction.invoke();
		}

		Optional<EntryPointMessageMapper> optional = _getMessageMapperOptional(
			request, _entryPointMessageMappers);

		return optional.map(Unsafe::unsafeCast);
	}

	/**
	 * Returns the error message mapper, if present, for the current request;
	 * {@code Optional#empty()} otherwise.
	 *
	 * @param  request the current request
	 * @param  computeEmptyFunction the function that can be called to compute
	 *         the data
	 * @return the error message mapper, if present; {@code Optional#empty()}
	 *         otherwise
	 */
	public Optional<ErrorMessageMapper> getErrorMessageMapperOptional(
		Request request, EmptyFunction computeEmptyFunction) {

		if (_errorMessageMappers == null) {
			computeEmptyFunction.invoke();
		}

		Optional<ErrorMessageMapper> optional = _getMessageMapperOptional(
			request, _errorMessageMappers);

		return optional.map(Unsafe::unsafeCast);
	}

	/**
	 * Returns the resource name's identifier class.
	 *
	 * @param  name the resource name
	 * @param  computeEmptyFunction the function that can be called to compute
	 *         the data
	 * @return the resource name's identifier class
	 */
	public <T extends Identifier> Optional<Class<T>> getIdentifierClassOptional(
		String name, EmptyFunction computeEmptyFunction) {

		if (_identifierClasses == null) {
			computeEmptyFunction.invoke();
		}

		return Optional.ofNullable(
			_identifierClasses
		).map(
			map -> map.get(name)
		).map(
			Unsafe::unsafeCast
		);
	}

	public Map<String, ItemRoutes> getItemRoutesMap(
		EmptyFunction computeEmptyFunction) {

		if (_itemRoutes == null) {
			computeEmptyFunction.invoke();
		}

		return _itemRoutes;
	}

	/**
	 * Returns the name of a collection resource that matches the specified
	 * class name.
	 *
	 * @param  className the collection resource's class name
	 * @param  computeEmptyFunction the function that can be called to compute
	 *         the data
	 * @return the collection resource's name
	 */
	public Optional<String> getNameOptional(
		String className, EmptyFunction computeEmptyFunction) {

		if (_names == null) {
			computeEmptyFunction.invoke();
		}

		Optional<Map<String, String>> optional = getNamesOptional();

		return optional.map(map -> map.get(className));
	}

	/**
	 * Returns the map containing the names for the different resource
	 * identifier classes, if they've been set; returns {@code Optional#empty()}
	 * otherwise.
	 *
	 * @return the map containing the names for the different resource
	 *         identifier classes, if they've been set; returns {@code
	 *         Optional#empty()} otherwise
	 */
	public Optional<Map<String, String>> getNamesOptional() {
		return Optional.ofNullable(_names);
	}

	public Map<String, NestedCollectionRoutes> getNestedCollectionRoutesMap(
		EmptyFunction computeEmptyFunction) {

		if (_nestedCollectionRoutes == null) {
			computeEmptyFunction.invoke();
		}

		return _nestedCollectionRoutes;
	}

	/**
	 * Returns the page message mapper, if present, for the current request;
	 * {@code Optional#empty()} otherwise.
	 *
	 * @param  request the current request
	 * @param  computeEmptyFunction the function that can be called to compute
	 *         the data
	 * @return the page message mapper, if present; {@code Optional#empty()}
	 *         otherwise
	 */
	public <T> Optional<PageMessageMapper<T>> getPageMessageMapperOptional(
		Request request, EmptyFunction computeEmptyFunction) {

		if (_pageMessageMappers == null) {
			computeEmptyFunction.invoke();
		}

		Optional<PageMessageMapper> optional = _getMessageMapperOptional(
			request, _pageMessageMappers);

		return optional.map(Unsafe::unsafeCast);
	}

	/**
	 * Returns the parsed types.
	 *
	 * @param  computeEmptyFunction the function that can be called to compute
	 *         the data
	 * @return the parsed type
	 * @review
	 */
	public Map<String, ParsedType> getParsedTypesMap(
		EmptyFunction computeEmptyFunction) {

		if (_parsedTypes == null) {
			computeEmptyFunction.invoke();
		}

		return _parsedTypes;
	}

	public Map<String, Representor> getRepresentorMap(
		EmptyFunction computeEmptyFunction) {

		if (_representors == null) {
			computeEmptyFunction.invoke();
		}

		return _representors;
	}

	/**
	 * Returns the representor, if present, of the collection resource's model
	 * class; {@code Optional#empty()} otherwise.
	 *
	 * @param  name the representor's name
	 * @param  computeEmptyFunction the function that can be called to compute
	 *         the data
	 * @return the representor, if present; {@code Optional#empty()} otherwise
	 */
	public <T> Optional<Representor<T>> getRepresentorOptional(
		String name, EmptyFunction computeEmptyFunction) {

		if (_representors == null) {
			computeEmptyFunction.invoke();
		}

		return Optional.ofNullable(
			_representors
		).map(
			map -> map.get(name)
		).map(
			Unsafe::unsafeCast
		);
	}

	public Map<String, NestedCollectionRoutes> getReusableCollectionRoutesMap(
		EmptyFunction computeEmptyFunction) {

		if (_reusableNestedCollectionRoutes == null) {
			computeEmptyFunction.invoke();
		}

		return _reusableNestedCollectionRoutes;
	}

	public Optional<Class<?>> getReusableIdentifierClassOptional(String name) {
		return Optional.ofNullable(
			_reusableIdentifierClasses
		).map(
			map -> map.get(name)
		).map(
			Unsafe::unsafeCast
		);
	}

	/**
	 * Returns the single model message mapper, if present, for the current
	 * request; {@code Optional#empty()} otherwise.
	 *
	 * @param  request the current request
	 * @param  computeEmptyFunction the function that can be called to compute
	 *         the data
	 * @return the single model message mapper, if present; {@code
	 *         Optional#empty()} otherwise
	 */
	public <T> Optional<SingleModelMessageMapper<T>>
		getSingleModelMessageMapperOptional(
			Request request, EmptyFunction computeEmptyFunction) {

		if (_singleModelMessageMappers == null) {
			computeEmptyFunction.invoke();
		}

		Optional<SingleModelMessageMapper> optional = _getMessageMapperOptional(
			request, _singleModelMessageMappers);

		return optional.map(Unsafe::unsafeCast);
	}

	/**
	 * Adds a batch result message mapper.
	 *
	 * @param mediaType the media type
	 * @param batchResultMessageMapper the batch result message mapper
	 */
	public void putBatchResultMessageMapper(
		MediaType mediaType,
		BatchResultMessageMapper batchResultMessageMapper) {

		if (_batchResultMessageMappers == null) {
			_batchResultMessageMappers = new HashMap<>();
		}

		_batchResultMessageMappers.put(mediaType, batchResultMessageMapper);
	}

	/**
	 * Adds collection routes.
	 *
	 * @param key the key
	 * @param collectionRoutes the collection routes
	 */
	public void putCollectionRoutes(
		String key, CollectionRoutes collectionRoutes) {

		if (_collectionRoutes == null) {
			_collectionRoutes = new HashMap<>();
		}

		_collectionRoutes.put(key, collectionRoutes);
	}

	public void putDocumentationContribution(
		CustomDocumentation customDocumentation) {

		_customDocumentation = customDocumentation;
	}

	/**
	 * Adds a documentation message mapper.
	 *
	 * @param mediaType the media type
	 * @param documentationMessageMapper the documentation message mapper
	 */
	public void putDocumentationMessageMapper(
		MediaType mediaType,
		DocumentationMessageMapper documentationMessageMapper) {

		if (_documentationMessageMappers == null) {
			_documentationMessageMappers = new HashMap<>();
		}

		_documentationMessageMappers.put(mediaType, documentationMessageMapper);
	}

	/**
	 * Adds a entry point message mapper.
	 *
	 * @param mediaType the media type
	 * @param entryPointMessageMapper the entry point message mapper
	 */
	public void putEntryPointMessageMapper(
		MediaType mediaType, EntryPointMessageMapper entryPointMessageMapper) {

		if (_entryPointMessageMappers == null) {
			_entryPointMessageMappers = new HashMap<>();
		}

		_entryPointMessageMappers.put(mediaType, entryPointMessageMapper);
	}

	/**
	 * Adds an error message mapper.
	 *
	 * @param mediaType the media type
	 * @param errorMessageMapper the error message mapper
	 */
	public void putErrorMessageMapper(
		MediaType mediaType, ErrorMessageMapper errorMessageMapper) {

		if (_errorMessageMappers == null) {
			_errorMessageMappers = new HashMap<>();
		}

		_errorMessageMappers.put(mediaType, errorMessageMapper);
	}

	/**
	 * Adds an identifier class.
	 *
	 * @param key the key
	 * @param identifierClass the identifier class
	 */
	public void putIdentifierClass(
		String key, Class<Identifier> identifierClass) {

		if (_identifierClasses == null) {
			_identifierClasses = new HashMap<>();
		}

		_identifierClasses.put(key, identifierClass);
	}

	/**
	 * Adds item routes.
	 *
	 * @param key the key
	 * @param itemRoutes the item routes
	 */
	public void putItemRoutes(String key, ItemRoutes itemRoutes) {
		if (_itemRoutes == null) {
			_itemRoutes = new HashMap<>();
		}

		_itemRoutes.put(key, itemRoutes);
	}

	/**
	 * Adds a resource name.
	 *
	 * @param key the key
	 * @param name the resource name
	 */
	public void putName(String key, String name) {
		if (_names == null) {
			_names = new HashMap<>();
		}

		_names.put(key, name);
	}

	/**
	 * Adds nested collection routes.
	 *
	 * @param key the key
	 * @param nestedCollectionRoutes the nested collection routes
	 */
	public void putNestedCollectionRoutes(
		String key, NestedCollectionRoutes nestedCollectionRoutes) {

		if (_nestedCollectionRoutes == null) {
			_nestedCollectionRoutes = new HashMap<>();
		}

		_nestedCollectionRoutes.put(key, nestedCollectionRoutes);
	}

	/**
	 * Adds a page message mapper.
	 *
	 * @param mediaType the media type
	 * @param pageMessageMapper the page message mapper
	 */
	public void putPageMessageMapper(
		MediaType mediaType, PageMessageMapper pageMessageMapper) {

		if (_pageMessageMappers == null) {
			_pageMessageMappers = new HashMap<>();
		}

		_pageMessageMappers.put(mediaType, pageMessageMapper);
	}

	/**
	 * Adds a parsed type.
	 *
	 * @param  key the key
	 * @param  parsedType the parsed type
	 * @review
	 */
	public void putParsedType(String key, ParsedType parsedType) {
		if (_parsedTypes == null) {
			_parsedTypes = new HashMap<>();
		}

		_parsedTypes.put(key, parsedType);
	}

	/**
	 * Adds a representor.
	 *
	 * @param key the key
	 * @param representor the representor
	 */
	public void putRepresentor(String key, Representor representor) {
		if (_representors == null) {
			_representors = new HashMap<>();
		}

		_representors.put(key, representor);
	}

	public void putReusableIdentifierClass(
		String key, Class<?> identifierClass) {

		if (_reusableIdentifierClasses == null) {
			_reusableIdentifierClasses = new HashMap<>();
		}

		_reusableIdentifierClasses.put(key, identifierClass);
	}

	/**
	 * Adds reusable nested collection routes.
	 *
	 * @param key the key
	 * @param reusableNestedCollectionRoutes the reusable nested collection
	 *        routes
	 */
	public void putReusableNestedCollectionRoutes(
		String key, NestedCollectionRoutes reusableNestedCollectionRoutes) {

		if (_reusableNestedCollectionRoutes == null) {
			_reusableNestedCollectionRoutes = new HashMap<>();
		}

		_reusableNestedCollectionRoutes.put(
			key, reusableNestedCollectionRoutes);
	}

	/**
	 * Adds a root resource name.
	 *
	 * @param  rootResourceNameSdk the root resource name
	 * @review
	 */
	public void putRootResourceNameSdk(String rootResourceNameSdk) {
		if (_rootResourceNameSdks == null) {
			_rootResourceNameSdks = new ArrayList<>();
		}

		_rootResourceNameSdks.add(rootResourceNameSdk);
	}

	/**
	 * Adds a single model message mapper.
	 *
	 * @param mediaType the media type
	 * @param singleModelMessageMapper the single model message mapper
	 */
	public void putSingleModelMessageMapper(
		MediaType mediaType,
		SingleModelMessageMapper singleModelMessageMapper) {

		if (_singleModelMessageMappers == null) {
			_singleModelMessageMappers = new HashMap<>();
		}

		_singleModelMessageMappers.put(mediaType, singleModelMessageMapper);
	}

	private ManagerCache() {
	}

	private <T> Optional<T> _getMessageMapperOptional(
		Request request, Map<MediaType, T> messageMappers) {

		return Optional.ofNullable(
			messageMappers
		).map(
			Map::keySet
		).map(
			Set::stream
		).map(
			stream -> stream.toArray(MediaType[]::new)
		).map(
			this::_getVariantListBuilder
		).map(
			VariantListBuilder::build
		).map(
			request::selectVariant
		).map(
			Variant::getMediaType
		).flatMap(
			mediaType -> Optional.ofNullable(
				messageMappers
			).map(
				map -> map.get(mediaType)
			)
		);
	}

	private VariantListBuilder _getVariantListBuilder(MediaType[] mediaTypes) {
		VariantListBuilder variantListBuilder = newInstance();

		List<MediaType> list = Arrays.asList(mediaTypes);

		if (list.contains(_MEDIA_TYPE)) {
			return variantListBuilder.mediaTypes(
				_MEDIA_TYPE
			).add(
			).mediaTypes(
				mediaTypes
			);
		}

		return variantListBuilder.mediaTypes(mediaTypes);
	}

	private static final MediaType _MEDIA_TYPE = new MediaType(
		"application", "hal+json");

	private List<ActionSemantics> _actionSemantics;
	private Map<MediaType, BatchResultMessageMapper> _batchResultMessageMappers;
	private Map<String, CollectionRoutes> _collectionRoutes;
	private CustomDocumentation _customDocumentation;
	private Map<MediaType, DocumentationMessageMapper>
		_documentationMessageMappers;
	private Map<MediaType, EntryPointMessageMapper> _entryPointMessageMappers;
	private Map<MediaType, ErrorMessageMapper> _errorMessageMappers;
	private Map<String, Class<Identifier>> _identifierClasses;
	private Map<String, ItemRoutes> _itemRoutes;
	private Map<String, String> _names;
	private Map<String, NestedCollectionRoutes> _nestedCollectionRoutes;
	private Map<MediaType, PageMessageMapper> _pageMessageMappers;
	private Map<String, ParsedType> _parsedTypes;
	private Map<String, Representor> _representors;
	private Map<String, Class<?>> _reusableIdentifierClasses;
	private Map<String, NestedCollectionRoutes> _reusableNestedCollectionRoutes;
	private List<String> _rootResourceNameSdks;
	private Map<MediaType, SingleModelMessageMapper> _singleModelMessageMappers;

}