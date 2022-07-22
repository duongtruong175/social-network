package vn.hust.socialnetwork.event;

import java.util.List;

import vn.hust.socialnetwork.models.story.Story;

public class StoryChangeEvent {
    private int positionList;
    private List<Story> newListStory;

    public StoryChangeEvent(int positionList, List<Story> newListStory) {
        this.positionList = positionList;
        this.newListStory = newListStory;
    }

    public int getPositionList() {
        return positionList;
    }

    public void setPositionList(int positionList) {
        this.positionList = positionList;
    }

    public List<Story> getNewListStory() {
        return newListStory;
    }

    public void setNewListStory(List<Story> newListStory) {
        this.newListStory = newListStory;
    }
}
