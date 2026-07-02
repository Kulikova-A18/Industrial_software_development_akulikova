package pages;

public class TestContext {

    private static Page currentPage;

    public static void setCurrentPage(Page page) {
        currentPage = page;
    }

    public static Page getCurrentPage() {
        return currentPage;
    }
}
