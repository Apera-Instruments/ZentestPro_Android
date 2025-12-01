package com.zen.api.data;

public class NoteData {


        private Long id;
        private String userId;
        private String noteId;//currentUserId+ noteName+timestamp
        private String noteName;
        private String noteAdmin;
        private String location;
        private String notes;
        private String attachedPhotos;//[url,...]
        private int noteStatus;
        private long updateTime;
        private long createTime;
        public long getCreateTime() {
            return this.createTime;
        }
        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }
        public long getUpdateTime() {
            return this.updateTime;
        }
        public void setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
        }
        public int getNoteStatus() {
            return this.noteStatus;
        }
        public void setNoteStatus(int noteStatus) {
            this.noteStatus = noteStatus;
        }
        public String getAttachedPhotos() {
            return this.attachedPhotos;
        }
        public void setAttachedPhotos(String attachedPhotos) {
            this.attachedPhotos = attachedPhotos;
        }
        public String getNotes() {
            return this.notes;
        }
        public void setNotes(String notes) {
            this.notes = notes;
        }
        public  void setLocation(String location){
            this.location = location;
        }
        public String getLocation(){
            return this.location;
        }
        public String getNoteAdmin() {
            return this.noteAdmin;
        }
        public void setNoteAdmin(String noteAdmin) {
            this.noteAdmin = noteAdmin;
        }
        public String getNoteName() {
            return this.noteName;
        }
        public void setNoteName(String noteName) {
            this.noteName = noteName;
        }
        public String getNoteId() {
            return this.noteId;
        }
        public void setNoteId(String noteId) {
            this.noteId = noteId;
        }
        public String getUserId() {
            return this.userId;
        }
        public void setUserId(String userId) {
            this.userId = userId;
        }
        public Long getId() {
            return this.id;
        }
        public void setId(Long id) {
            this.id = id;
        }

        public NoteData() {
        }

}
