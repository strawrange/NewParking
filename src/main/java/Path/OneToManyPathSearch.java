/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package Path;

import org.matsim.contrib.dvrp.path.OneToManyPathSearch.PathData;
import com.google.common.collect.Maps;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.locationchoice.router.BackwardFastMultiNodeDijkstra;
import org.matsim.contrib.locationchoice.router.BackwardFastMultiNodeDijkstraFactory;
import org.matsim.contrib.locationchoice.router.BackwardMultiNodePathCalculator;
import org.matsim.core.router.FastMultiNodeDijkstraFactory;
import org.matsim.core.router.InitialNode;
import org.matsim.core.router.MultiNodePathCalculator;
import org.matsim.core.router.RoutingNetworkImaginaryNode;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OneToManyPathSearch {
	public static OneToManyPathSearch createForwardSearch(Network network, TravelTime travelTime,
                                                               TravelDisutility travelDisutility) {
		return create((MultiNodePathCalculator)new FastMultiNodeDijkstraFactory(true).createPathCalculator(network,
				travelDisutility, travelTime));
	}

	public static OneToManyPathSearch createBackwardSearch(Network network, TravelTime travelTime,
                                                                TravelDisutility travelDisutility) {
		return create((BackwardMultiNodePathCalculator)new BackwardFastMultiNodeDijkstraFactory(true)
				.createPathCalculator(network, travelDisutility, travelTime));
	}

	public static OneToManyPathSearch create(MultiNodePathCalculator multiNodeDijkstra) {
		return new OneToManyPathSearch(multiNodeDijkstra);
	}

	public static class DrtPathData extends PathData {
		double pathDistance = 0.0;
		final Path path;

		public DrtPathData(Path path, double firstAndLastLinkTT) {
			super(path, firstAndLastLinkTT);
			this.path = path;
			for (Link link : path.links){
				pathDistance += link.getLength();
			}
		}

		public double getPathDistance(){
			return pathDistance;
		}

	}

	private static class ToNode extends InitialNode {
		private Path path;

		private ToNode(Node node, double initialCost, double initialTime) {
			super(node, initialCost, initialTime);
		}
	}

	private final MultiNodePathCalculator multiNodeDijkstra;// forward or backward
	private final boolean forward;

	private OneToManyPathSearch(MultiNodePathCalculator multiNodeDijkstra) {
		this.multiNodeDijkstra = multiNodeDijkstra;
		this.forward = !(multiNodeDijkstra instanceof BackwardFastMultiNodeDijkstra);
	}

	public DrtPathData[] calcPathDataArray(Link fromLink, List<Link> toLinks, double startTime) {
		Node fromNode = getFromNode(fromLink);
		Map<Id<Node>, ToNode> toNodes = createToNodes(fromLink, toLinks);
		calculatePaths(fromNode, toNodes, startTime);
		return createPathDataArray(fromLink, toLinks, startTime, toNodes);
	}

	public Map<Id<Link>, DrtPathData> calcPathDataMap(Link fromLink, Collection<Link> toLinks, double startTime) {
		Node fromNode = getFromNode(fromLink);
		Map<Id<Node>, ToNode> toNodes = createToNodes(fromLink, toLinks);
		calculatePaths(fromNode, toNodes, startTime);
		return createPathDataMap(fromLink, toLinks, startTime, toNodes);
	}

	private Map<Id<Node>, ToNode> createToNodes(Link fromLink, Collection<Link> toLinks) {
		Map<Id<Node>, ToNode> toNodes = Maps.newHashMapWithExpectedSize(toLinks.size());
		for (Link toLink : toLinks) {
			if (toLink != fromLink) {
				Node toNode = getToNode(toLink);
				toNodes.putIfAbsent(toNode.getId(), new ToNode(toNode, 0, 0));
			}
		}
		return toNodes;
	}

	private void calculatePaths(Node fromNode, Map<Id<Node>, ToNode> toNodes, double startTime) {
		RoutingNetworkImaginaryNode imaginaryNode = new RoutingNetworkImaginaryNode(toNodes.values());
		multiNodeDijkstra.setSearchAllEndNodes(true);
		multiNodeDijkstra.calcLeastCostPath(fromNode, imaginaryNode, startTime, null, null);

		// get path for each ToNode

		// XXX in most cases we need costs/times, while paths could be constructed lazily only when needed
		// TODO add getCost/Time() to MultiNodeDijkstra
		for (ToNode toNode : toNodes.values()) {
			toNode.path = multiNodeDijkstra.constructPath(fromNode, toNode.node, startTime);
		}
	}

	private DrtPathData[] createPathDataArray(Link fromLink, List<Link> toLinks, double startTime,
			Map<Id<Node>, ToNode> toNodes) {
		DrtPathData[] pathDataArray = new DrtPathData[toLinks.size()];
		for (int i = 0; i < pathDataArray.length; i++) {
			pathDataArray[i] = createPathData(fromLink, toLinks.get(i), startTime, toNodes);
		}
		return pathDataArray;
	}

	private Map<Id<Link>, DrtPathData> createPathDataMap(Link fromLink, Collection<Link> toLinks, double startTime,
			Map<Id<Node>, ToNode> toNodes) {
		Map<Id<Link>, DrtPathData> pathDataMap = Maps.newHashMapWithExpectedSize(toLinks.size());
		for (Link toLink : toLinks) {
			pathDataMap.put(toLink.getId(), createPathData(fromLink, toLink, startTime, toNodes));
		}
		return pathDataMap;
	}

	private DrtPathData createPathData(Link fromLink, Link toLink, double startTime, Map<Id<Node>, ToNode> toNodes) {
		if (toLink == fromLink) {
			return createZeroPathData(fromLink);
		} else {
			ToNode toNode = toNodes.get(getToNode(toLink).getId());
			return new DrtPathData(toNode.path, getFirstAndLastLinkTT(fromLink, toLink, toNode.path, startTime));
		}
	}

	private DrtPathData createZeroPathData(Link fromLink) {
		List<Node> singleNodeList = Collections.singletonList(getFromNode(fromLink));
		List<Link> emptyLinkList = Collections.emptyList();
		return new DrtPathData(new Path(singleNodeList, emptyLinkList, 0, 0), 0);
	}

	private Node getToNode(Link toLink) {
		return forward ? toLink.getFromNode() : toLink.getToNode();
	}

	private Node getFromNode(Link fromLink) {
		return forward ? fromLink.getToNode() : fromLink.getFromNode();
	}

	private double getFirstAndLastLinkTT(Link fromLink, Link toLink, Path path, double time) {
		double lastLinkTT = forward ? //
				VrpPaths.getLastLinkTT(toLink, time + path.travelTime) : VrpPaths.getLastLinkTT(fromLink, time);
		return VrpPaths.FIRST_LINK_TT + lastLinkTT;
	}
}
