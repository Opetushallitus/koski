describe('Muokkauspalkki', function( ){
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

    describe('Näkyvyys', function() {
        before(page.openPage, page.oppijaHaku.searchAndSelect('020655-2479'))
        it('piilossa näyttötilassa', function() {
            expect(isVisible("#edit-bar")).to.equal(false)
        })

        it('näkyvillä muokkaustilassa', function() {
            click("button.toggle-edit")
            expect(isVisible("#edit-bar")).to.equal(true)
        })

        it('piilossa muokkaustilasta poistuttaessa', function() {
            click("a.cancel")
            expect(isVisible("#edit-bar")).to.equal(false)
        })


        it('piilossa oppijataulukosta näyttötilaan edellinen-painikkeella palattaessa', function(done) {
            click("a.back-link")
            expect(currentURL().endsWith("/koski/")).to.equal(true)
            expect(hasClass("#topbar + div", "oppijataulukko")).to.equal(true)
            goBack()
            setTimeout(() => {
                try {
                    expect(currentURL().endsWith("/koski/")).to.equal(false)
                    expect(isVisible("#edit-bar")).to.equal(false)
                }
                catch (err) {
                    done(err)
                    return
                }
                done()
            }, 0)
        })

        it('näkyvillä muokkaustilaan palattaessa', function() {
            click("button.toggle-edit")
            expect(isVisible("#edit-bar")).to.equal(true)
        })

        it('näkyvillä oppijataulukosta muokkaustilaan edellinen-painikkeella palattaessa', function(done) {
            click("a.back-link")
            expect(currentURL().endsWith("/koski/")).to.equal(true)
            expect(hasClass("#topbar + div", "oppijataulukko")).to.equal(true)
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
