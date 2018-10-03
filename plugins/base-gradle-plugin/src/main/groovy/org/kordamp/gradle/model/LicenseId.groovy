/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.gradle.model

import groovy.transform.CompileStatic

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
enum LicenseId {
    AFL_2_1('AFL-2.1'),
    AFL_3_0('AFL-3.0'),
    AGPL_V3('AGPL-V3'),
    APACHE_1_0('Apache-1.0'),
    APACHE_1_1('Apache-1.1'),
    APACHE_2_0('Apache-2.0'),
    APL_1_0('APL-1.0'),
    APSL_2_0('APSL-2.0'),
    ARTISTIC_LICENSE_2_0('Artistic-License-2.0'),
    ATTRIBUTION('Attribution'),
    BOUNCY_CASTLE('Bouncy-Castle'),
    BSD('BSD'),
    BSD_2_CLAUSE('BSD 2-Clause'),
    BSD_3_CLAUSE('BSD 3-Clause'),
    BSL_1_0('BSL-1.0'),
    CA_TOSL_1_1('CA-TOSL-1.1'),
    CC0_1_0('CC0-1.0'),
    CDDL_1_0('CDDL-1.0'),
    CODEHAUS('Codehaus'),
    CPAL_1_0('CPAL-1.0'),
    CPL_1_0('CPL-1.0'),
    CPOL_1_02('CPOL-1.02'),
    CUAOFFICE_1_0('CUAOFFICE-1.0'),
    DAY('Day'),
    DAY_ADDENDUM('Day-Addendum'),
    ECL2('ECL2'),
    EIFFEL_2_0('Eiffel-2.0'),
    ENTESSA_1_0('Entessa-1.0'),
    EPL_1_0('EPL-1.0'),
    EPL_2_0('EPL-2.0'),
    EUDATAGRID('EUDATAGRID'),
    EUPL_1_1('EUPL-1.1'),
    EUPL_1_2('EUPL-1.2'),
    FAIR('Fair'),
    FACEBOOK_PLATFORM('Facebook-Platform'),
    FRAMEWORX_1_0('Frameworx-1.0'),
    GO('Go'),
    GPL_2_0('GPL-2.0'),
    GPL_2_0_CE('GPL-2.0+CE'),
    GPL_3_0('GPL-3.0'),
    HISTORICAL('Historical'),
    HSQLDB('HSQLDB'),
    IBMPL_1_0('IBMPL-1.0'),
    IJG('IJG'),
    IMAGEMAGICK('ImageMagick'),
    IPAFONT_1_0('IPAFont-1.0'),
    ISC('ISC'),
    IU_EXTREME_1_1_1('IU-Extreme-1.1.1'),
    JA_SIG('JA-SIG'),
    JSON('JSON'),
    JTIDY('JTidy'),
    LGPL_2_0('LGPL-2.0'),
    LGPL_2_1('LGPL-2.1'),
    LGPL_3_0('LGPL-3.0'),
    LIBPNG('Libpng'),
    LPPL_1_0('LPPL-1.0'),
    LUCENT_1_02('Lucent-1.02'),
    MIROS('MirOS'),
    MIT('MIT'),
    MOTOSOTO_0_9_1('Motosoto-0.9.1'),
    MOZILLA_1_1('Mozilla-1.1'),
    MPL_2_0('MPL-2.0'),
    MS_PL('MS-PL'),
    MS_RL('MS-RL'),
    MULTICS('Multics'),
    NASA_1_3('NASA-1.3'),
    NAUMEN('NAUMEN'),
    NCSA('NCSA'),
    NETHACK('Nethack'),
    NOKIA_1_0A('Nokia-1.0a'),
    NOSL_3_0('NOSL-3.0'),
    NTP('NTP'),
    NUNIT_2_6_3('NUnit-2.6.3'),
    NUNIT_TEST_ADAPTER_2_6_3('NUnit-Test-Adapter-2.6.3'),
    OCLC_2_0('OCLC-2.0'),
    OPENFONT_1_1('Openfont-1.1'),
    OPENGROUP('Opengroup'),
    OPENSSL('OpenSSL'),
    OSL_3_0('OSL-3.0'),
    PHP_3_0('PHP-3.0'),
    POSTGRESQL('PostgreSQL'),
    PUBLIC_DOMAIN('Public Domain'),
    PUBLIC_DOMAIN_SUN('Public Domain - SUN'),
    PYTHONPL('PythonPL'),
    PYTHONSOFTFOUNDATION('PythonSoftFoundation'),
    QTPL_1_0('QTPL-1.0'),
    REAL_1_0('Real-1.0'),
    RICOHPL('RicohPL'),
    RPL_1_5('RPL-1.5'),
    SCALA('Scala'),
    SIMPL_2_0('SimPL-2.0'),
    SLEEPYCAT('Sleepycat'),
    SUNPUBLIC_1_0('SUNPublic-1.0'),
    SYBASE_1_0('Sybase-1.0'),
    TMATE('TMate'),
    UNICODE_DFS_2015('Unicode-DFS-2015'),
    UNLICENSE('Unlicense'),
    UOI_NCSA('UoI-NCSA'),
    UPL_1_0('UPL-1.0'),
    VIM_LICENSE('VIM License'),
    VOVIDAPL_1_0('VovidaPL-1.0'),
    W3C('W3C'),
    WTFPL('WTFPL'),
    WXWINDOWS('wxWindows'),
    XNET('Xnet'),
    ZLIB('ZLIB'),
    ZPL_2_0('ZPL-2.0');

    private String shortName

    LicenseId(String shortName) {
        this.shortName = shortName
    }

    String shortName() {
        shortName
    }
}
