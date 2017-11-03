/*
 * Copyright (C) 2011-2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file is auto-generated. DO NOT MODIFY!
 * The source Renderscript file: C:\\Users\\FiMobile-JMK\\Documents\\GIT\\app\\src\\main\\rs\\histogram_compute.rs
 */

package net.sourceforge.opencamera;

import android.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_histogram_compute extends ScriptC {
    private static final String __rs_resource_name = "histogram_compute";
    // Constructor
    public  ScriptC_histogram_compute(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_histogram_compute(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __U8_4;
    private final static int mExportVarIdx_histogram = 0;
    private Allocation mExportVar_histogram;
    public void bind_histogram(Allocation v) {
        mExportVar_histogram = v;
        if (v == null) bindAllocation(null, mExportVarIdx_histogram);
        else bindAllocation(v, mExportVarIdx_histogram);
    }

    public Allocation get_histogram() {
        return mExportVar_histogram;
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_histogram_compute = 1;
    public Script.KernelID getKernelID_histogram_compute() {
        return createKernelID(mExportForEachIdx_histogram_compute, 57, null, null);
    }

    public void forEach_histogram_compute(Allocation ain) {
        forEach_histogram_compute(ain, null);
    }

    public void forEach_histogram_compute(Allocation ain, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        forEach(mExportForEachIdx_histogram_compute, ain, null, null, sc);
    }

    private final static int mExportFuncIdx_init_histogram = 0;
    public void invoke_init_histogram() {
        invoke(mExportFuncIdx_init_histogram);
    }

}

