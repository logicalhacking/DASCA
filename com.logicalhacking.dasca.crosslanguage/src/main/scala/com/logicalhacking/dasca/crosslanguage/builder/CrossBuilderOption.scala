/*
 * (C) Copyright 2010-2015 SAP SE.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package com.logicalhacking.dasca.crosslanguage.builder

sealed trait CrossBuilderOption {}

case object MockCordovaExec extends CrossBuilderOption
case object ReplacePluginDefinesAndRequires extends CrossBuilderOption
case object FilterJavaCallSites extends CrossBuilderOption
case object FilterJSFrameworks extends CrossBuilderOption
case object PreciseJS extends CrossBuilderOption
case object RunBuildersInParallel extends CrossBuilderOption
