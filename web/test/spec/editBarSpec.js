describe('EditBar', function( ){
    function currentURL() {
        return testFrame().location.href
    }
    
    function hasClass(selector, className) {
        return S(selector).attr("class").split(" ").includes(className)
    }

    function isVisible(selector) {
        return hasClass(selector, "visible")
    }

    function click(selector) {
        triggerEvent(S(selector)[0], "click")
    }
    
    var page = KoskiPage()
    before(Authentication().login(), resetFixtures)

    describe('Visibility', function() {
        before(page.openPage, page.oppijaHaku.searchAndSelect('020655-2479'))
        it('hidden by default', function() {
            expect(isVisible("#edit-bar")).to.equal(false)
        })

        it('visible on edit mode', function() {
            click("button.toggle-edit")
            expect(isVisible("#edit-bar")).to.equal(true)
        })

        it('hidden when exiting edit mode', function() {
            click("a.cancel")
            expect(isVisible("#edit-bar")).to.equal(false)
        })

        it('visible when entering edit mode again', function() {
            click("button.toggle-edit")
            expect(isVisible("#edit-bar")).to.equal(true)
        })

        it('returns to oppijataulukko with back link', function() {
            click("a.back-link")
            expect(currentURL().endsWith("/koski/")).to.equal(true)
            expect(hasClass("#topbar + div", "oppijataulukko")).to.equal(true)
        })

        it('shows correctly when returning to edit mode with back button', function(done) {
            goBack()
            setTimeout(() => {
                try {
                    expect(currentURL().endsWith("/koski/")).to.equal(false)
                    expect(isVisible("#edit-bar")).to.equal(true)
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
