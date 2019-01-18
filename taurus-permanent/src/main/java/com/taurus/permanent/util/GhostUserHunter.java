package com.taurus.permanent.util;

import com.taurus.core.util.Logger;
import com.taurus.permanent.TaurusPermanent;
import com.taurus.permanent.core.SessionManager;
import com.taurus.permanent.core.SystemController;
import com.taurus.permanent.data.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * GhostUserHunter
 * 
 * @author daixiwei
 *
 */
public class GhostUserHunter {
	private static final String		EOL				= System.getProperty("line.separator");
	private final TaurusPermanent	taurus;
	private SessionManager			sm;
	private SystemController				controller;
	private final Logger			log;
	private static final int		TOT_CYCLES		= 2;
	private int						cycleCounter	= 0;

	public GhostUserHunter() {
		taurus = TaurusPermanent.getInstance();
		this.controller = TaurusPermanent.getInstance().getController();
		log = Logger.getLogger(getClass());
	}

	public void hunt() {
		if (sm == null) {
			sm = taurus.getSessionManager();
		}

		if (++cycleCounter < TOT_CYCLES) {
			return;
		}
		cycleCounter = 0;

		List<Session> ghosts = searchGhosts();
		if (ghosts.size() > 0) {
			log.info(buildReport(ghosts));
		}

		for (Session ghost : ghosts)
			controller.disconnect(ghost);
	}

	private List<Session> searchGhosts() {
		List<Session> allUsers = taurus.getSessionManager().getAllSessions();
		List<Session> ghosts = new ArrayList<Session>();

		for (Session u : allUsers) {
			Session sess = u;

			if ((!sm.containsSession(sess)) || (sess.isIdle()) || (sess.isMarkedForEviction())) {
				ghosts.add(u);
			}
		}
		return ghosts;
	}

	private String buildReport(List<Session> ghosts) {
		StringBuilder sb = new StringBuilder("GHOST REPORT");
		sb.append(EOL).append("Total ghosts: ").append(ghosts.size()).append(EOL);

		for (Session ghost : ghosts) {
			Session ss = ghost;

			if (ss == null) {
				sb.append(ghost.getId()).append(", ").append(" -> Null session").append(", SessionById: ").append(this.sm.getSessionById(ghost.getId()));
			} else {
				sb.append(ghost.getId()).append(", ").append(", Connected: ").append(ss.isConnected()).append(", Idle: ").append(ss.isIdle()).append(", Marked: ")
						.append(ss.isMarkedForEviction()).append(", Frozen: ").append(ss.isFrozen()).append(", SessionById: ").append(this.sm.getSessionById(ghost.getId()));
			}
		}
		return sb.toString();
	}
}
