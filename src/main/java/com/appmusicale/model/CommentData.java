package com.appmusicale.model;

import java.util.List;
import java.util.Map;

public record CommentData(List<Comment> topLevelComments, Map<Integer, List<Comment>> commentTree) {

}
