/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.rest.dsl;

import java.util.List;
import java.util.stream.Collectors;

import org.onap.aai.rest.enums.EdgeDirection;

public class Edge {

    private List<EdgeLabel> labels;
    private EdgeDirection direction;

    public Edge(EdgeDirection direction, List<EdgeLabel> labels) {
        this.labels = labels;
        this.direction = direction;
    }

    public List<EdgeLabel> getLabels() {
        return labels;
    }

    public void setLabels(List<EdgeLabel> labels) {
        this.labels = labels;
    }

    public EdgeDirection getDirection() {
        return direction;
    }

    public void setDirection(EdgeDirection direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "labels: %s, direction: %s ".formatted(
            labels.stream().map(EdgeLabel::getLabel).collect(Collectors.joining(",")),
            this.getDirection().name());
    }
}
