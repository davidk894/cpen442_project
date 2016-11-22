package cpen442.securefileshare;

public class Job {
    private int JobType; // Got key, pending response, or pending request
    private String fileHash;
    private String userID;
    private String jobID;

    public int getJobType() {
        return JobType;
    }

    public void setJobType(int jobType) { jobType = jobType; }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        fileHash = fileHash;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        userID = userID;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        jobID = jobID;
    }

}
