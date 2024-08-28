/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.testware.usat.page.object

import com.ericsson.oss.testware.usat.page.ability.ActionBarAbility
import com.ericsson.oss.testware.usat.page.ability.DialogBoxAbility
import com.ericsson.oss.testware.usat.page.ability.InteractionAbility
import com.ericsson.oss.testware.usat.page.ability.WaitAbility

trait PageObject implements InteractionAbility, WaitAbility, ActionBarAbility, DialogBoxAbility {

}