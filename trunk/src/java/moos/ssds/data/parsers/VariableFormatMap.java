/*
 * Copyright 2009 MBARI
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1 
 * (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/copyleft/lesser.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package moos.ssds.data.parsers;

import java.util.HashMap;

/**
 * <p>
 * This is a HACK to help map data format types defined in
 * moos.ssds.model.RecordVariable to netcdf
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.3 $
 */
public class VariableFormatMap extends HashMap {

    /**
     * Singleton
     */
    protected VariableFormatMap() {
        put("byte", byte.class);
        put("short", short.class);
        put("integer", int.class);
        put("int", int.class);
        put("long", long.class);
        put("float", float.class);
        put("float4", float.class);
        put("float8", double.class);
        put("double", double.class);
        put("%s", new String().getClass());
        put("string", new String().getClass());
        put("String", new String().getClass());
        put("datetime", new String().getClass());
        put("%i", int.class);
        put("%f", float.class);
        put("%d", double.class);
    }

    public static VariableFormatMap getInstance() {
        if (instance == null) {
            instance = new VariableFormatMap();
        }
        return instance;
    }

    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */
    private static VariableFormatMap instance = null;
}