import math
from datetime import datetime

import numpy as np
import pandas as pd
import requests


def convert_to_statistics(df):
    # calculate change percentage
    df_change = df.transpose().pct_change()
    df_change = df_change.replace(np.inf, 1)

    data = []
    for country in df.index:
        stat = {'country': country, 'values': {}}
        for date in list(df.columns.values):
            formatted_date = datetime.strptime(date, '%m/%d/%y').strftime('%Y-%m-%d')
            value = df[date][country]
            value_changed = df_change[country][date]

            stat['values'][formatted_date] = {
                'value': int(value),
                'pctChange': float(value_changed) if not math.isnan(value_changed) else None
            }
        data.append(stat)
    return data


def load_sick(confirmed, deaths, recovered):
    return confirmed - deaths - recovered


def load_file(file):
    df = pd.read_csv(f'{source_url}/{file}')
    return df.set_index('country')


def send_data(data, target):
    url = f'{target_server}{target}/python'
    responses = [requests.post(url, json=json).status_code for json in data]
    print(target, {i: responses.count(i) for i in responses})


if __name__ == '__main__':
    source_url = 'https://raw.githubusercontent.com/jjbeto/talkPy4JavaDevResources/master/johns_hopkins'
    confirmed_file = 'time_series_covid19_confirmed_global.csv'
    deaths_file = 'time_series_covid19_deaths_global.csv'
    recovered_file = 'time_series_covid19_recovered_global.csv'

    target_server = 'http://localhost:8080/api/'

    confirmed = load_file(confirmed_file)
    deaths = load_file(deaths_file)
    recovered = load_file(recovered_file)
    sick = load_sick(confirmed, deaths, recovered)

    confirmed_json = convert_to_statistics(confirmed)
    deaths_json = convert_to_statistics(deaths)
    recovered_json = convert_to_statistics(recovered)
    sick_json = convert_to_statistics(sick)

    send_data(confirmed_json, 'confirmed')
    send_data(deaths_json, 'deaths')
    send_data(recovered_json, 'recovered')
    send_data(sick_json, 'sick')
