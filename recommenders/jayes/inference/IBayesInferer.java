/**
 * Copyright (c) 2011 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Michael Kutschke - initial API and implementation.
 */
package robozin.recommenders.jayes.inference;

import java.util.Map;

import robozin.recommenders.jayes.BayesNet;
import robozin.recommenders.jayes.BayesNode;

public interface IBayesInferer {

    void setNetwork(BayesNet bayesNet);

    void setEvidence(Map<BayesNode, String/* outcome */> evidence);

    void addEvidence(BayesNode node, String outcome);

    Map<BayesNode, String> getEvidence();

    double[] getBeliefs(BayesNode node);

}
