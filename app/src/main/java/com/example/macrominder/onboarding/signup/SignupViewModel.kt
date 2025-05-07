package com.example.macrominder.onboarding.signup

import androidx.lifecycle.ViewModel

class SignupViewModel : ViewModel() {
    var gender: String? = null
    var birthday: String? = null
    var height: String? = null
    var weight: String? = null
    var activityLevel: Int? = null
    var activityLevelDescription: String? = null
    var customActivityValue: String? = null
    var weightGoal: Int? = null

}
