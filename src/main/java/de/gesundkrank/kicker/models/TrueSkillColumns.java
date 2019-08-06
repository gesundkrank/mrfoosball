/*
 * This file is part of kicker (https://github.com/mbrtargeting/kicker).
 * Copyright (c) 2019 Jan Graßegger.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.gesundkrank.kicker.models;

import javax.persistence.MappedSuperclass;

import de.gesundkrank.jskills.Rating;

import de.gesundkrank.kicker.trueskill.TrueSkillCalculator;

@MappedSuperclass
public class TrueSkillColumns {

    public Double trueSkillMean = TrueSkillCalculator.DEFAULT_INITIAL_MEAN;
    public Double trueSkillStandardDeviation =
            TrueSkillCalculator.DEFAULT_INITIAL_STANDARD_DEVIATION;

    public void updateRating(final Rating newRating) {
        trueSkillMean = newRating.getMean();
        trueSkillStandardDeviation = newRating.getStandardDeviation();
    }
}
