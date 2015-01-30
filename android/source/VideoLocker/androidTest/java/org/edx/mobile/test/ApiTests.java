package org.edx.mobile.test;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.VideoResponseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class contains unit tests for API calls to server.
 * 
 */
public class ApiTests extends BaseTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        login();
    }
    
    public void testSyncLastSubsection() throws Exception {
        Api api = new Api(getInstrumentation().getTargetContext());
        
        EnrolledCoursesResponse e = api.getEnrolledCourses().get(0);
        Map<String, SectionEntry> map = api.getCourseHierarchy(e.getCourse().getId());
        Entry<String, SectionEntry> entry = map.entrySet().iterator().next();
        Entry<String, ArrayList<VideoResponseModel>> subsection = entry.getValue().sections.entrySet().iterator().next();
        
        String courseId = e.getCourse().getId();
        String lastVisitedModuleId = subsection.getValue().get(0).getSection().id;

        assertNotNull(courseId);
        assertNotNull(lastVisitedModuleId);
        
        print(String.format("course= %s ; sub-section= %s", courseId, lastVisitedModuleId));
        
        // TODO: lastVisitedModuleId must be section.id (id is now available)
        
        
        SyncLastAccessedSubsectionResponse model = api.syncLastAccessedSubsection(courseId, lastVisitedModuleId);
        assertNotNull(model);
        print("sync returned: " + model.last_visited_module_id);
    }
    
    public void testGetLastAccessedModule() throws Exception {
        Api api = new Api(getInstrumentation().getTargetContext());
        
        EnrolledCoursesResponse e = api.getEnrolledCourses().get(0); 
        
        String courseId = e.getCourse().getId();
        assertNotNull(courseId);
        
        print(String.format("course= %s", courseId));
        
        SyncLastAccessedSubsectionResponse model = api.getLastAccessedSubsection(courseId);
        assertNotNull(model);
    //  print(model.json);
    }
    
    public void testResetPassword() throws Exception {
        print("test: reset password");

        Api api = new Api(getInstrumentation().getTargetContext());
        ResetPasswordResponse model = api.resetPassword("user@edx.org");
        assertTrue(model != null);
        print(model.value);
        print("test: finished: reset password");
    }
    
    public void testHandouts() throws Exception {
        Api api = new Api(getInstrumentation().getTargetContext());

        // get a course id for this test
        List<EnrolledCoursesResponse> courses = api.getEnrolledCourses();
        assertTrue("Must have enrolled to at least one course",
                courses != null && courses.size() > 0);
        String handoutURL = courses.get(0).getCourse().getCourse_handouts();

        HandoutModel model = api.getHandout(handoutURL, false);
        assertTrue(model != null);
        print(model.handouts_html);
    }
    
    public void testCourseStructure() throws Exception {
        Api api = new Api(getInstrumentation().getTargetContext());

        // get a course id for this test
        List<EnrolledCoursesResponse> courses = api.getEnrolledCourses();
        assertTrue("Must have enrolled to at least one course",
                courses != null && courses.size() > 0);
        String courseId = courses.get(0).getCourse().getId();

        Map<String, SectionEntry> chapters = api.getCourseHierarchy(courseId);
        for(Entry<String, SectionEntry> entry : chapters.entrySet()) {
            print("---------------" + entry.getKey() + "---------------");
            for (Entry<String, ArrayList<VideoResponseModel>> se : entry.getValue().sections.entrySet()) {
                print("------------" + se.getKey() + "------------");
                for (VideoResponseModel v : se.getValue()) {
                    print(v.getSummary().getName());
                }
            }
        }
    }
    
    public void login() throws Exception {
        Api api = new Api(getInstrumentation().getTargetContext());
        AuthResponse res = api.auth("user@edx.org", "*****");
        assertNotNull(res);
        assertNotNull(res.access_token);
        assertNotNull(res.token_type);
        print(res.toString());

        ProfileModel profile = api.getProfile();
        assertNotNull(profile);
    }

    public void testGetAnnouncement() throws Exception {
        Api api = new Api(getInstrumentation().getTargetContext());

        // get a course id for this test
        List<EnrolledCoursesResponse> courses = api.getEnrolledCourses();
        assertTrue("Must have enrolled to at least one course",
                courses != null && courses.size() > 0);
        String updatesUrl = courses.get(0).getCourse().getCourse_updates();

        List<AnnouncementsModel> res = api.getAnnouncement(updatesUrl, false);
        assertTrue(res != null);
        for (AnnouncementsModel r : res) {
            print(r.getDate());
        }
    }
}
