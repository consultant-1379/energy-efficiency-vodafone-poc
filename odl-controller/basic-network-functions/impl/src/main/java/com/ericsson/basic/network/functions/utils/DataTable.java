/*
 * Copyright (c) 2017 Ericsson, AB.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.ericsson.basic.network.functions.inventory.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ericsson
 */

public class DataTable {

    private static final Logger LOG = LoggerFactory.getLogger(DataTable.class);

    private int numRow;
    private int numCol;
    private int[][] data;

    public DataTable(final int numRow, final int numCol, final int initValue) {
        data = new int[numRow][numCol];
        for (int i = 0; i < numRow; i++) {
            for (int j = 0; j < numCol; j++) {
                data[i][j] = initValue;
            }
        }
        this.numRow = numRow;
        this.numCol = numCol;
    }

    public boolean setValue(final int rowIdx, final int colIdx, final int value) {
        if (rowIdx >= numRow || colIdx >= numCol) {
            return false;
        }
        data[rowIdx][colIdx] = value;
        return true;
    }

    public int getValue(final int rowIdx, final int colIdx) throws ArrayIndexOutOfBoundsException {
        if (rowIdx >= numRow) {
            throw new ArrayIndexOutOfBoundsException(rowIdx);
        }
        if (colIdx >= numCol) {
            throw new ArrayIndexOutOfBoundsException(colIdx);
        }

        return data[rowIdx][colIdx];
    }

    public int getNumRow() {
        return numRow;
    }

    public int getNumCol() {
        return numCol;
    }
}

