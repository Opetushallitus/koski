describe('EditBar', function( ){
    var page = KoskiPage()
    before(Authentication().login(), resetFixtures)

    describe('Visibility', function() {
        before(page.openPage, page.oppijaHaku.searchAndSelect('020655-2479'))
        it('hidden by default', function() {
            expect(S("#edit-bar").attr("class").split(" ").includes("visible")).to.equal(false)
        })

        it('visible on edit mode', function() {
            triggerEvent(S("button.toggle-edit")[0], "click")
            expect(S("#edit-bar").attr("class").split(" ").includes("visible")).to.equal(true)
        })

        it('hidden when exiting edit mode', function() {
            triggerEvent(S("a.cancel")[0], "click")
            expect(S("#edit-bar").attr("class").split(" ").includes("visible")).to.equal(false)
        })

        it('visible when entering edit mode again', function() {
            triggerEvent(S("button.toggle-edit")[0], "click")
            expect(S("#edit-bar").attr("class").split(" ").includes("visible")).to.equal(true)
        })

        it('returns to oppijataulukko with back link', function() {
            triggerEvent(S("a.back-link")[0], "click")
            expect(testFrame().location.href.endsWith("/koski/")).to.equal(true)
            expect(S("#topbar + div").first().attr("class").split(" ").includes("oppijataulukko")).to.equal(true)
        })

        it('shows correctly when returning to edit mode with back button', function(done) {
            goBack()
            setTimeout(() => {
                try {
                    expect(testFrame().location.href.endsWith("/koski/")).to.equal(false)
                    expect(S("#edit-bar").attr("class").split(" ").includes("visible")).to.equal(true)
                }
                catch (err) {
                    done(err)
                    return
                }
                done()
            }, 0)
        })
    })
})
